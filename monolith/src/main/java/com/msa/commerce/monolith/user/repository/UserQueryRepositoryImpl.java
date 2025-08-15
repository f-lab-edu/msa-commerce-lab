package com.msa.commerce.monolith.user.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.msa.commerce.common.util.QuerydslPageableSort;
import com.msa.commerce.monolith.user.UserStatus;
import com.msa.commerce.monolith.user.entity.QUser;
import com.msa.commerce.monolith.user.entity.User;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

public class UserQueryRepositoryImpl implements UserQueryRepository {

    private final JPAQueryFactory query;

    public UserQueryRepositoryImpl(JPAQueryFactory query) {
        this.query = query;
    }

    @Override
    public Page<User> search(String keyword, UserStatus status, LocalDateTime createdFrom, LocalDateTime createdTo,
        Pageable pageable) {

        QUser u = QUser.user;

        var where = new com.querydsl.core.types.Predicate[] {
            likeKeyword(keyword),
            eqStatus(status),
            gteCreatedAt(createdFrom),
            ltCreatedAt(createdTo)
        };

        List<User> content = query
            .selectFrom(u)
            .where(where)
            .orderBy(QuerydslPageableSort.toOrderSpecifiers(u, pageable.getSort()))
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        Long totalBoxed = query
            .select(u.id.count())   // u.count() 대신 id.count()도 동일하게 동작
            .from(u)
            .where(where)
            .fetchOne();

        long total = (totalBoxed != null) ? totalBoxed : 0L;

        return new PageImpl<>(content, pageable, total);
    }

    private BooleanExpression likeKeyword(String keyword) {
        QUser u = QUser.user;
        if (keyword == null || keyword.isBlank())
            return null;
        String like = "%" + keyword.trim() + "%";
        return u.username.likeIgnoreCase(like)
            .or(u.email.likeIgnoreCase(like))
            .or(u.firstName.likeIgnoreCase(like))
            .or(u.lastName.likeIgnoreCase(like));
    }

    private BooleanExpression eqStatus(UserStatus status) {
        return status == null ? null : QUser.user.status.eq(status);
    }

    private BooleanExpression gteCreatedAt(LocalDateTime from) {
        return from == null ? null : QUser.user.createdAt.goe(from);
    }

    private BooleanExpression ltCreatedAt(LocalDateTime to) {
        return to == null ? null : QUser.user.createdAt.lt(to);
    }

}
