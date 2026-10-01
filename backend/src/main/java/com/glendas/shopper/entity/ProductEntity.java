package com.glendas.shopper.entity;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * A row in {@code products} (V2). Not exposed over HTTP: controllers map it to the
 * contract's {@code ProductDetail}, so the database shape and the API shape can change independently.
 *
 * <p>There's deliberately <b>no {@code setStockLevel}</b>. Stock is set once when the product is
 * created, and after that changes only through the conditional SQL update for stock
 * adjustments (ADR-008, T-017) and orders (003). That way nobody can accidentally write a
 * stale stock value and undo a concurrent sale (004 AC-28).
 */
@Entity
@Table(name = "products")
public class ProductEntity {

    /** We generate the id in Java ({@code UUID.randomUUID()}), so no {@code @GeneratedValue}. */
    @Id
    private UUID id;

    private String name;

    /**
     * {@code lower(trim(name))}, computed by PostgreSQL (V2). Mapped <b>read-only</b>
     * ({@code insertable = false, updatable = false}): Hibernate never writes it, but repository
     * queries can still search on it, e.g. {@code existsByNameKey}.
     */
    @Column(name = "name_key", insertable = false, updatable = false)
    private String nameKey;

    private String description;

    /**
     * Many products → one category. {@code LAZY} means the category row is only loaded when
     * {@link #getCategory()} is actually used. {@code @JoinColumn} names the foreign key column.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id")
    private CategoryEntity category;

    /** {@code BigDecimal}, never {@code double}: exact decimal values, like C#'s {@code decimal}. */
    @Column(precision = 9, scale = 2)
    private BigDecimal unitAmount;

    /** One of g, kg, ml, l, each. The database CHECK constraint rejects anything else. */
    private String unitMeasure;

    /** Integer pence (ADR-005). */
    private int pricePence;

    private String imageUrl;

    private int stockLevel;

    protected ProductEntity() {
    }

    /** A new product. The only way to set the starting stock (004 AC-18, AC-20). */
    public ProductEntity(UUID id, String name, String description, CategoryEntity category,
                         BigDecimal unitAmount, String unitMeasure, int pricePence, String imageUrl,
                         int startingStock) {
        this.id = id;
        this.stockLevel = startingStock;
        updateDetails(name, description, category, unitAmount, unitMeasure, pricePence, imageUrl);
    }

    /**
     * Edit everything except stock (004 AC-21). Inside a {@code @Transactional} service method
     * there's no "save" call needed: Hibernate notices changed fields when the transaction ends
     * and writes them ("dirty checking", like EF Core's change tracking + SaveChanges).
     */
    public void updateDetails(String name, String description, CategoryEntity category,
                              BigDecimal unitAmount, String unitMeasure, int pricePence, String imageUrl) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.unitAmount = unitAmount;
        this.unitMeasure = unitMeasure;
        this.pricePence = pricePence;
        this.imageUrl = imageUrl;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getNameKey() {
        return nameKey;
    }

    public String getDescription() {
        return description;
    }

    public CategoryEntity getCategory() {
        return category;
    }

    public BigDecimal getUnitAmount() {
        return unitAmount;
    }

    public String getUnitMeasure() {
        return unitMeasure;
    }

    public int getPricePence() {
        return pricePence;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public int getStockLevel() {
        return stockLevel;
    }
}
