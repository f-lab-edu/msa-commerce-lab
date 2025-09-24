package com.msa.commerce.monolith.product.domain.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.msa.commerce.monolith.product.adapter.out.persistence.ProductCategoryJpaEntity;
import com.msa.commerce.monolith.product.adapter.out.persistence.ProductCategoryJpaRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductCategoryDomainService {

    private final ProductCategoryJpaRepository categoryRepository;

    public ProductCategoryJpaEntity createCategory(String name, String description, String slug,
                                                 ProductCategoryJpaEntity parent, Integer displayOrder,
                                                 Boolean isActive, Boolean isFeatured, String imageUrl) {

        validateCategoryCreation(name, slug, parent);

        // 동일 부모 하에서 최대 표시 순서 + 1 설정
        Integer nextDisplayOrder = displayOrder;
        if (nextDisplayOrder == null) {
            nextDisplayOrder = getNextDisplayOrder(parent);
        }

        ProductCategoryJpaEntity category = ProductCategoryJpaEntity.builder()
            .parent(parent)
            .name(name)
            .description(description)
            .slug(slug)
            .displayOrder(nextDisplayOrder)
            .isActive(isActive != null ? isActive : true)
            .isFeatured(isFeatured != null ? isFeatured : false)
            .imageUrl(imageUrl)
            .build();

        ProductCategoryJpaEntity savedCategory = categoryRepository.save(category);

        log.info("카테고리 생성 완료 - ID: {}, 이름: {}, 부모: {}",
                savedCategory.getId(), savedCategory.getName(),
                parent != null ? parent.getId() : "ROOT");

        return savedCategory;
    }

    public void moveCategory(Long categoryId, ProductCategoryJpaEntity newParent) {
        ProductCategoryJpaEntity category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다: " + categoryId));

        validateCategoryMove(category, newParent);

        ProductCategoryJpaEntity oldParent = category.getParent();

        // 부모 변경
        category.changeParent(newParent);

        // 새로운 부모에서의 표시 순서 조정
        Integer nextDisplayOrder = getNextDisplayOrder(newParent);
        category.updateDisplayOrder(nextDisplayOrder);

        categoryRepository.save(category);

        log.info("카테고리 이동 완료 - ID: {}, 기존 부모: {}, 새 부모: {}",
                categoryId,
                oldParent != null ? oldParent.getId() : "ROOT",
                newParent != null ? newParent.getId() : "ROOT");
    }

    public void reorderCategories(ProductCategoryJpaEntity parent, List<Long> categoryIds) {
        List<ProductCategoryJpaEntity> categories = new ArrayList<>();

        // 지정된 순서대로 카테고리 조회
        for (int i = 0; i < categoryIds.size(); i++) {
            Long categoryId = categoryIds.get(i);
            ProductCategoryJpaEntity category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다: " + categoryId));

            // 같은 부모인지 확인
            if (!isSameParent(category.getParent(), parent)) {
                throw new IllegalArgumentException(
                    String.format("카테고리 %d는 다른 부모를 가지고 있습니다.", categoryId));
            }

            category.updateDisplayOrder(i + 1);
            categories.add(category);
        }

        categoryRepository.saveAll(categories);

        log.info("카테고리 순서 조정 완료 - 부모: {}, 개수: {}",
                parent != null ? parent.getId() : "ROOT", categories.size());
    }

    public void deleteCategory(Long categoryId, boolean moveChildrenToParent) {
        ProductCategoryJpaEntity category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다: " + categoryId));

        if (category.hasChildren()) {
            if (moveChildrenToParent) {
                // 하위 카테고리를 상위로 이동
                ProductCategoryJpaEntity parent = category.getParent();
                for (ProductCategoryJpaEntity child : category.getChildren()) {
                    child.changeParent(parent);
                    categoryRepository.save(child);
                }
                log.info("하위 카테고리들을 상위로 이동 - 삭제 카테고리: {}, 하위 개수: {}",
                        categoryId, category.getChildren().size());
            } else {
                throw new IllegalArgumentException("하위 카테고리가 존재하여 삭제할 수 없습니다. 하위 카테고리를 먼저 삭제하거나 이동해주세요.");
            }
        }

        categoryRepository.delete(category);
        log.info("카테고리 삭제 완료 - ID: {}, 이름: {}", categoryId, category.getName());
    }

    @Transactional(readOnly = true)
    public List<CategoryTreeNode> getCategoryTree() {
        List<ProductCategoryJpaEntity> allCategories = categoryRepository.findAll();

        // 부모별로 그룹화
        Map<Long, List<ProductCategoryJpaEntity>> childrenByParentId = new HashMap<>();
        List<ProductCategoryJpaEntity> rootCategories = new ArrayList<>();

        for (ProductCategoryJpaEntity category : allCategories) {
            if (category.getParent() == null) {
                rootCategories.add(category);
            } else {
                childrenByParentId
                    .computeIfAbsent(category.getParent().getId(), k -> new ArrayList<>())
                    .add(category);
            }
        }

        // 트리 구조 생성
        return rootCategories.stream()
            .sorted((c1, c2) -> Integer.compare(c1.getDisplayOrder(), c2.getDisplayOrder()))
            .map(root -> buildCategoryTreeNode(root, childrenByParentId))
            .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductCategoryJpaEntity> getCategoryAncestors(Long categoryId) {
        ProductCategoryJpaEntity category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다: " + categoryId));

        return category.getAllAncestors();
    }

    @Transactional(readOnly = true)
    public List<ProductCategoryJpaEntity> getCategoryDescendants(Long categoryId) {
        return categoryRepository.findAllSubCategories(categoryId);
    }

    @Transactional(readOnly = true)
    public String getCategoryPath(Long categoryId) {
        ProductCategoryJpaEntity category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다: " + categoryId));

        return category.getFullPath();
    }

    @Transactional(readOnly = true)
    public List<ProductCategoryJpaEntity> getFeaturedCategories() {
        return categoryRepository.findByIsActiveAndIsFeaturedOrderByDisplayOrderAsc(true, true);
    }

    private void validateCategoryCreation(String name, String slug, ProductCategoryJpaEntity parent) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("카테고리명은 필수입니다.");
        }

        if (slug == null || slug.trim().isEmpty()) {
            throw new IllegalArgumentException("슬러그는 필수입니다.");
        }

        // 동일 부모 하에서 이름 중복 검사
        Long parentId = parent != null ? parent.getId() : null;
        if (categoryRepository.existsByParentIdAndName(parentId, name)) {
            throw new IllegalArgumentException("동일한 부모 카테고리에 같은 이름이 이미 존재합니다: " + name);
        }

        // 전체 슬러그 중복 검사
        if (categoryRepository.existsBySlug(slug)) {
            throw new IllegalArgumentException("슬러그가 이미 존재합니다: " + slug);
        }
    }

    private void validateCategoryMove(ProductCategoryJpaEntity category, ProductCategoryJpaEntity newParent) {
        // 자기 자신을 부모로 설정하려는 경우
        if (newParent != null && category.getId().equals(newParent.getId())) {
            throw new IllegalArgumentException("자기 자신을 부모 카테고리로 설정할 수 없습니다.");
        }

        // 하위 카테고리를 상위 카테고리로 설정하려는 경우 (순환 참조 방지)
        if (newParent != null && isDescendantOf(newParent, category)) {
            throw new IllegalArgumentException("하위 카테고리를 상위 카테고리로 이동할 수 없습니다. 순환 참조가 발생합니다.");
        }

        // 최대 깊이 제한 (예: 5단계)
        if (newParent != null && newParent.getDepth() >= 4) {
            throw new IllegalArgumentException("카테고리 깊이는 최대 5단계까지 허용됩니다.");
        }
    }

    private boolean isDescendantOf(ProductCategoryJpaEntity potentialDescendant, ProductCategoryJpaEntity ancestor) {
        if (potentialDescendant == null || ancestor == null) {
            return false;
        }

        ProductCategoryJpaEntity current = potentialDescendant.getParent();
        while (current != null) {
            if (current.getId().equals(ancestor.getId())) {
                return true;
            }
            current = current.getParent();
        }
        return false;
    }

    private Integer getNextDisplayOrder(ProductCategoryJpaEntity parent) {
        Integer maxDisplayOrder;
        if (parent != null) {
            maxDisplayOrder = categoryRepository.findMaxDisplayOrderByParentId(parent.getId());
        } else {
            maxDisplayOrder = categoryRepository.findMaxDisplayOrderForRootCategories();
        }
        return maxDisplayOrder != null ? maxDisplayOrder + 1 : 1;
    }

    private boolean isSameParent(ProductCategoryJpaEntity parent1, ProductCategoryJpaEntity parent2) {
        if (parent1 == null && parent2 == null) {
            return true;
        }
        if (parent1 == null || parent2 == null) {
            return false;
        }
        return parent1.getId().equals(parent2.getId());
    }

    private CategoryTreeNode buildCategoryTreeNode(ProductCategoryJpaEntity category,
                                                 Map<Long, List<ProductCategoryJpaEntity>> childrenByParentId) {
        List<ProductCategoryJpaEntity> children = childrenByParentId.getOrDefault(category.getId(), new ArrayList<>());

        List<CategoryTreeNode> childNodes = children.stream()
            .sorted((c1, c2) -> Integer.compare(c1.getDisplayOrder(), c2.getDisplayOrder()))
            .map(child -> buildCategoryTreeNode(child, childrenByParentId))
            .toList();

        return CategoryTreeNode.builder()
            .category(category)
            .children(childNodes)
            .depth(category.getDepth())
            .childCount(children.size())
            .build();
    }

    @lombok.Builder
    @lombok.Getter
    public static class CategoryTreeNode {
        private final ProductCategoryJpaEntity category;
        private final List<CategoryTreeNode> children;
        private final int depth;
        private final int childCount;

        public boolean hasChildren() {
            return children != null && !children.isEmpty();
        }

        public boolean isLeaf() {
            return !hasChildren();
        }
    }
}