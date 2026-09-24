package com.quanlychitieu.doan.category;

public class CategoryModel {

    private final long id;
    private final String name;
    private final String type;
    private final String icon;
    private final String color;
    private final boolean defaultCategory;
    private final boolean createButton;

    public CategoryModel(
            long id,
            String name,
            String type,
            String icon,
            String color,
            boolean defaultCategory
    ) {
        this(
                id,
                name,
                type,
                icon,
                color,
                defaultCategory,
                false
        );
    }

    private CategoryModel(
            long id,
            String name,
            String type,
            String icon,
            String color,
            boolean defaultCategory,
            boolean createButton
    ) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.icon = icon;
        this.color = color;
        this.defaultCategory = defaultCategory;
        this.createButton = createButton;
    }

    public static CategoryModel createButton(String type) {
        return new CategoryModel(
                -1,
                "Tạo",
                type,
                "",
                "#F9C74F",
                false,
                true
        );
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getIcon() {
        return icon;
    }

    public String getColor() {
        return color;
    }

    public boolean isDefaultCategory() {
        return defaultCategory;
    }

    public boolean isCreateButton() {
        return createButton;
    }
}