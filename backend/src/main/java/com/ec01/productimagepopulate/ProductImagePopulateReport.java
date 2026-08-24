package com.ec01.productimagepopulate;

import java.util.List;

record ProductImagePopulateReport(int discovered, int uploaded, List<ProductImageFailure> failures) {
}

record ProductImageFailure(Long productId, String productName, String keyword, String reason) {
}
