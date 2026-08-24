package com.ec01.productimagepopulate;

record ProductImageCandidate(
        String title,
        String imageUrl,
        String sourcePage,
        String creator,
        String license,
        String licenseUrl,
        int width,
        int height
) {
}
