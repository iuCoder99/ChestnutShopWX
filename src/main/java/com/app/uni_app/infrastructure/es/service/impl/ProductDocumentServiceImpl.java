package com.app.uni_app.infrastructure.es.service.impl;

import com.app.uni_app.infrastructure.es.document.ProductDocument;
import com.app.uni_app.infrastructure.es.repository.ProductEsRepository;
import com.app.uni_app.infrastructure.es.service.ProductDocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class ProductDocumentServiceImpl implements ProductDocumentService {

    private final ProductEsRepository productEsRepository;

    @Override
    public ProductDocument getProductDocumentById(Long id) {
        if (Objects.isNull(id)) {
            return null;
        }
        return productEsRepository.getById(id);
    }

    @Override
    public List<ProductDocument> getProductDocumentById(Long id, Long... ids) {
        return productEsRepository.getById(id, ids);
    }

    @Override
    public List<ProductDocument> getProductDocumentByIdList(List<Long> idList) {
        if (Objects.isNull(idList)||idList.isEmpty()){
            return Collections.emptyList();
        }
       return productEsRepository.getByIdList(idList);
    }

    @Override
    public void saveProductDocument(ProductDocument productDocument) {
        productEsRepository.save(productDocument);
    }

    @Override
    public void batchSaveProductDocument(List<ProductDocument> productDocumentList) {
        productEsRepository.batchSave(productDocumentList);
    }
}
