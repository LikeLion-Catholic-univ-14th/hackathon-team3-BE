package com.example.hackathon_team3_be.api;

import com.example.hackathon_team3_be.common.NotFoundException;
import com.example.hackathon_team3_be.domain.ExperienceSession;
import com.example.hackathon_team3_be.domain.Product;
import com.example.hackathon_team3_be.repository.ExperienceSessionRepository;
import com.example.hackathon_team3_be.repository.ExperienceProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.HtmlUtils;

import java.nio.charset.StandardCharsets;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/assets")
public class AssetController {

    private static final MediaType SVG = new MediaType("image", "svg+xml", StandardCharsets.UTF_8);

    private final ExperienceSessionRepository sessionRepository;
    private final ExperienceProductRepository productRepository;

    @GetMapping("/unseen/{unseenId}.svg")
    ResponseEntity<String> unseen(@PathVariable String unseenId) {
        ExperienceSession session = sessionRepository.findByUnseenPublicId(unseenId)
                .orElseThrow(() -> new NotFoundException("UNSEEN 이미지를 찾을 수 없습니다."));
        String label = HtmlUtils.htmlEscape(session.getUnseenPublicId());
        String detail = HtmlUtils.htmlEscape(session.getIntentSignature());
        String fill = colorCode(session.getColor());
        return ResponseEntity.ok().contentType(SVG).body(bagSvg(label, detail, fill));
    }

    @GetMapping("/products/{sku}.svg")
    ResponseEntity<String> product(@PathVariable String sku) {
        Product product = productRepository.findAll().stream()
                .filter(item -> item.getSku().equalsIgnoreCase(sku))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("상품 이미지를 찾을 수 없습니다."));
        return ResponseEntity.ok().contentType(SVG).body(bagSvg(
                HtmlUtils.htmlEscape(product.getName()),
                HtmlUtils.htmlEscape(product.getStructureType() + " / " + product.getColor()),
                colorCode(product.getColor())
        ));
    }

    private String bagSvg(String label, String detail, String fill) {
        return """
                <svg xmlns="http://www.w3.org/2000/svg" width="1200" height="800" viewBox="0 0 1200 800">
                  <rect width="1200" height="800" fill="#11100f"/>
                  <path d="M390 300 Q600 85 810 300" fill="none" stroke="#d8c1a8" stroke-width="28" stroke-linecap="round"/>
                  <rect x="290" y="270" width="620" height="360" rx="95" fill="%s" stroke="#f2e9df" stroke-width="9"/>
                  <path d="M310 405 Q600 535 890 405" fill="none" stroke="#f2e9df" stroke-opacity=".5" stroke-width="7"/>
                  <rect x="570" y="390" width="60" height="60" transform="rotate(45 600 420)" fill="#e9783f"/>
                  <text x="600" y="700" fill="#f7f3ed" text-anchor="middle" font-family="serif" font-size="42">%s</text>
                  <text x="600" y="750" fill="#b8afa6" text-anchor="middle" font-family="sans-serif" font-size="22">%s</text>
                </svg>
                """.formatted(fill, label, detail);
    }

    private String colorCode(String color) {
        String normalized = color == null ? "" : color.toLowerCase();
        if (normalized.contains("cognac") || normalized.contains("브라운")) return "#9b5f36";
        if (normalized.contains("black") || normalized.contains("블랙")) return "#2d2a28";
        if (normalized.contains("cream") || normalized.contains("neutral") || normalized.contains("뉴트럴")) return "#c9b9a5";
        if (normalized.contains("red") || normalized.contains("레드")) return "#8f2f2f";
        return "#8f755f";
    }
}
