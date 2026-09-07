package com.e.commerce.mini.DTO.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class WishlistResponseDTO {

    private Long wishlistId;

    private List<WishlistItemResponseDTO> items;

    private List<WishlistItemResponseDTO> wishlist;

    private Integer totalItems;
}