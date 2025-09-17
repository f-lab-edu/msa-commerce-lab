package com.msa.commerce.monolith.product.adapter.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductCategoryJpaRepository extends JpaRepository<ProductCategoryJpaEntity, Long> {

    List<ProductCategoryJpaEntity> findByIsActiveTrue();

    List<ProductCategoryJpaEntity> findByParentIsNullAndIsActiveTrue();

    List<ProductCategoryJpaEntity> findByParentIdAndIsActiveTrue(Long parentId);

    Optional<ProductCategoryJpaEntity> findBySlug(String slug);

    List<ProductCategoryJpaEntity> findByIsFeaturedTrueAndIsActiveTrueOrderByDisplayOrderAsc();

    List<ProductCategoryJpaEntity> findByIsActiveAndIsFeaturedOrderByDisplayOrderAsc(Boolean isActive, Boolean isFeatured);

    @Query("""
        SELECT pc FROM ProductCategoryJpaEntity pc
        LEFT JOIN FETCH pc.children c
        WHERE pc.parent IS NULL
        AND pc.isActive = true
        ORDER BY pc.displayOrder ASC, pc.name ASC
        """)
    List<ProductCategoryJpaEntity> findRootCategoriesWithChildren();

    @Query("""
        SELECT pc FROM ProductCategoryJpaEntity pc
        LEFT JOIN FETCH pc.parent p
        LEFT JOIN FETCH pc.children c
        WHERE pc.id = :id
        """)
    Optional<ProductCategoryJpaEntity> findByIdWithParentAndChildren(@Param("id") Long id);

    @Query("""
        SELECT pc FROM ProductCategoryJpaEntity pc
        WHERE pc.parent.id = :categoryId
        OR pc.id IN (
            SELECT c.id FROM ProductCategoryJpaEntity c
            WHERE c.parent.id IN (
                SELECT sc.id FROM ProductCategoryJpaEntity sc WHERE sc.parent.id = :categoryId
            )
        )
        """)
    List<ProductCategoryJpaEntity> findAllSubCategories(@Param("categoryId") Long categoryId);

    @Query("""
        SELECT pc FROM ProductCategoryJpaEntity pc
        WHERE pc.name LIKE %:keyword%
        AND pc.isActive = true
        ORDER BY pc.displayOrder ASC
        """)
    List<ProductCategoryJpaEntity> searchByName(@Param("keyword") String keyword);

    boolean existsBySlug(String slug);

    boolean existsByParentIdAndName(Long parentId, String name);

    @Query("""
        SELECT CASE WHEN COUNT(pc) > 0 THEN true ELSE false END
        FROM ProductCategoryJpaEntity pc
        WHERE pc.slug = :slug
        AND pc.id <> :excludeId
        """)
    boolean existsBySlugAndIdNot(@Param("slug") String slug, @Param("excludeId") Long excludeId);

    @Query("""
        SELECT MAX(pc.displayOrder) FROM ProductCategoryJpaEntity pc
        WHERE pc.parent.id = :parentId
        """)
    Integer findMaxDisplayOrderByParentId(@Param("parentId") Long parentId);

    @Query("""
        SELECT MAX(pc.displayOrder) FROM ProductCategoryJpaEntity pc
        WHERE pc.parent IS NULL
        """)
    Integer findMaxDisplayOrderForRootCategories();

}
