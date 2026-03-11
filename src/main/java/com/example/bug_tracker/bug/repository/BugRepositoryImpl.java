package com.example.bug_tracker.bug.repository;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.example.bug_tracker.bug.domain.BugPriority;
import com.example.bug_tracker.bug.domain.BugStatus;
import com.example.bug_tracker.bug.entity.BugEntity;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

// custom repository の実装本体
@Repository
public class BugRepositoryImpl implements BugRepositoryCustom {

    // JPAの動的クエリ作成の入口
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<BugEntity> search(
            BugStatus status,
            BugPriority priority,
            String keyword,
            Pageable pageable) {

        // 条件付き検索用のBuilderを取得
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        // ----------------------------
        // 1. データ取得用クエリ
        // ----------------------------

        // BugEntityを返す検索クエリの設計図
        CriteriaQuery<BugEntity> cq = cb.createQuery(BugEntity.class);

        // From BugEntity に相当する主語
        Root<BugEntity> bug = cq.from(BugEntity.class);

        // WHERE句の検索条件を格納するリスト
        List<Predicate> predicates = new ArrayList<>();

        // status が指定されていれば「status = ?」を追加
        if (status != null) {
            predicates.add(cb.equal(bug.get("status"), status));
        }

        // priority が指定されていれば「priority = ?」を追加
        if (priority != null) {
            predicates.add(cb.equal(bug.get("priority"), priority));
        }

        // keyword が指定されていれば「title LIKE ? OR description LIKE ?」を追加
        if (keyword != null) {
            // LIKE検索用のワイルドカード付き小文字キーワードを作成
            String likeValue = "%" + keyword.toLowerCase() + "%";

            // 「Lower(title) LIKE `%keyword%`」に相当
            Predicate titleLike = cb.like(cb.lower(bug.get("title")), likeValue);

            // 「Lower(description) LIKE `%keyword%`」に相当
            Predicate descriptionLike = cb.like(cb.lower(bug.get("description")), likeValue);

            // (titleLike OR descriptionLike) を1つの条件として追加する
            predicates.add(cb.or(titleLike, descriptionLike));
        }

        // 追加された条件をANDでまとめ、WHERE句にセット
        cq.where(predicates.toArray(new Predicate[0]));

        // createdAtの降順で並べる。ORDER BY句に相当
        cq.orderBy(cb.desc(bug.get("createdAt")));

        // 設計図から実行可能クエリを作る
        TypedQuery<BugEntity> query = entityManager.createQuery(cq);

        // ページング開始位置・1ページの取得件数を設定する
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        // クエリを実行して結果を取得
        List<BugEntity> content = query.getResultList();

        // ----------------------------
        // 2. 総件数取得用クエリ
        // ----------------------------

        // 総件数取得用のクエリ設計図
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);

        // countクエリ用のFROM句の主語
        Root<BugEntity> countBug = countQuery.from(BugEntity.class);

        // countクエリ用のWHERE句条件リスト
        List<Predicate> countPredicates = new ArrayList<>();

        // status条件をcountクエリに追加
        if (status != null) {
            countPredicates.add(cb.equal(countBug.get("status"), status));
        }

        // priority条件をcountクエリに追加
        if (priority != null) {
            countPredicates.add(cb.equal(countBug.get("priority"), priority));
        }

        // keyword条件をcountクエリに追加
        if (keyword != null) {
            String likeValue = "%" + keyword.toLowerCase() + "%";

            Predicate titleLike = cb.like(cb.lower(countBug.get("title")), likeValue);

            Predicate descriptionLike = cb.like(cb.lower(countBug.get("description")), likeValue);

            countPredicates.add(cb.or(titleLike, descriptionLike));
        }

        // 「SELECT COUNT(*)」に相当
        countQuery.select(cb.count(countBug));

        // WHERE句に検索条件をセット
        countQuery.where(countPredicates.toArray(new Predicate[0]));

        // 総件数を取得
        Long total = entityManager.createQuery(countQuery).getSingleResult();

        // count + pageable + total をPageへ詰めて返す
        return new PageImpl<>(content, pageable, total);
    }
}