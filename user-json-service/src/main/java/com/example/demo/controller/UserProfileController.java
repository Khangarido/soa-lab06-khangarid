package com.example.demo.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.UserProfile;
import com.example.demo.repository.UserProfileRepository;

// REST controller - Hereglegchiin profile CRUD uildluud
@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "*")
public class UserProfileController {

    private static final Logger log = LoggerFactory.getLogger(UserProfileController.class);

    private final UserProfileRepository profileRepository;

    public UserProfileController(UserProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    // Buh hereglegchdiin jagsaalt авах
    @GetMapping
    public ResponseEntity<List<UserProfile>> getAllProfiles() {
        log.info("[REST] GET /users - Buh profile jagsaalt");
        List<UserProfile> profiles = profileRepository.findAll();
        log.info("[REST] {} profile oldsn", profiles.size());
        return ResponseEntity.ok(profiles);
    }

    // ID-aar hereglegch haih
    @GetMapping("/{id}")
    public ResponseEntity<UserProfile> getProfileById(@PathVariable Long id) {
        log.info("[REST] GET /users/{} - Profile haih", id);
        return profileRepository.findById(id)
                .map(p -> {
                    log.info("[REST] Profile oldson: {} ({})", p.getName(), p.getUserId());
                    return ResponseEntity.ok(p);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Shine profile uusgeh
    @PostMapping
    public ResponseEntity<UserProfile> createProfile(@RequestBody UserProfile profile) {
        log.info("[REST] POST /users - Shine profile uusgeh: {}", profile.getName());
        UserProfile saved = profileRepository.save(profile);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // Profile shinechleh
    @PutMapping("/{id}")
    public ResponseEntity<UserProfile> updateProfile(@PathVariable Long id,
                                                     @RequestBody UserProfile updated) {
        log.info("[REST] PUT /users/{} - Profile shinechleh", id);
        return profileRepository.findById(id)
                .map(existing -> {
                    existing.setName(updated.getName());
                    existing.setEmail(updated.getEmail());
                    existing.setBio(updated.getBio());
                    existing.setPhone(updated.getPhone());
                    UserProfile saved = profileRepository.save(existing);
                    log.info("[REST] Profile amjilttai shinechlegdlee: {}", saved.getName());
                    return ResponseEntity.ok(saved);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Profile ustgah
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProfile(@PathVariable Long id) {
        log.info("[REST] DELETE /users/{} - Profile ustgah", id);
        if (profileRepository.existsById(id)) {
            profileRepository.deleteById(id);
            log.info("[REST] Profile ustgagdlaa: id={}", id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
