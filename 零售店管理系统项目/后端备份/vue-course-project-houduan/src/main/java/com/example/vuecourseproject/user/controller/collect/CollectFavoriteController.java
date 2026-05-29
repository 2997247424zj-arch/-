package com.example.vuecourseproject.user.controller.collect;

import com.example.vuecourseproject.user.entity.collect.CollectFavorite;
import com.example.vuecourseproject.user.service.collect.CollectFavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/favorites")
public class CollectFavoriteController {

    @Autowired
    private CollectFavoriteService favoriteService;

    @PostMapping("/{productId}")
    public ResponseEntity<?> addFavorite(
            @PathVariable Long productId,
            @RequestParam String username) {
        favoriteService.addFavorite(productId, username);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<?> removeFavorite(
            @PathVariable Long productId,
            @RequestParam String username) {
        favoriteService.removeFavorite(productId, username);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<CollectFavorite>> getFavorites(
            @RequestParam String username,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        List<CollectFavorite> favorites = favoriteService.getFavorites(username, keyword, page, size);
        return ResponseEntity.ok(favorites);
    }

    @DeleteMapping
    public ResponseEntity<?> clearFavorites(@RequestParam String username) {
        favoriteService.clearFavorites(username);
        return ResponseEntity.ok().build();
    }
}