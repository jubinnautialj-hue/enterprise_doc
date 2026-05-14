package com.enterprise.doc.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.enterprise.doc.entity.Document;
import com.enterprise.doc.mapper.DocumentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ElasticsearchService {

    public static final String INDEX_NAME = "documents";

    private final ElasticsearchClient elasticsearchClient;
    private final DocumentMapper documentMapper;
    private final ContentExtractService contentExtractService;

    @PostConstruct
    public void init() {
        try {
            if (!elasticsearchClient.indices().exists(e -> e.index(INDEX_NAME)).value()) {
                elasticsearchClient.indices().create(c -> c
                        .index(INDEX_NAME)
                        .mappings(m -> m
                                .properties("id", p -> p.long_(l -> l))
                                .properties("name", p -> p.text(t -> t))
                                .properties("content", p -> p.text(t -> t))
                                .properties("libraryId", p -> p.long_(l -> l))
                                .properties("folderId", p -> p.long_(l -> l))
                                .properties("fileType", p -> p.keyword(k -> k))
                                .properties("fileExtension", p -> p.keyword(k -> k))
                                .properties("createUserId", p -> p.long_(l -> l))
                                .properties("createUserName", p -> p.text(t -> t))
                                .properties("createTime", p -> p.date(d -> d))
                        )
                );
                log.info("Elasticsearch index '{}' created successfully", INDEX_NAME);
            }
        } catch (IOException e) {
            log.warn("Failed to create Elasticsearch index, continuing without it", e);
        }
    }

    public void indexDocument(Document document) {
        try {
            String content = contentExtractService.extractText(document.getStoragePath());
            
            Map<String, Object> doc = new HashMap<>();
            doc.put("id", document.getId());
            doc.put("name", document.getName());
            doc.put("content", content);
            doc.put("libraryId", document.getLibraryId());
            doc.put("folderId", document.getFolderId());
            doc.put("fileType", document.getFileType());
            doc.put("fileExtension", document.getFileExtension());
            doc.put("createUserId", document.getCreateUserId());
            doc.put("createUserName", document.getCreateUserName());
            doc.put("createTime", document.getCreateTime() != null ? document.getCreateTime().toString() : null);

            elasticsearchClient.index(i -> i
                    .index(INDEX_NAME)
                    .id(String.valueOf(document.getId()))
                    .document(doc)
            );
            log.info("Document indexed: {}", document.getId());
        } catch (Exception e) {
            log.warn("Failed to index document: {}", document.getId(), e);
        }
    }

    public void deleteDocument(Long id) {
        try {
            elasticsearchClient.delete(d -> d
                    .index(INDEX_NAME)
                    .id(String.valueOf(id))
            );
            log.info("Document deleted from index: {}", id);
        } catch (Exception e) {
            log.warn("Failed to delete document from index: {}", id, e);
        }
    }

    public List<Long> searchDocuments(String keyword, Long libraryId) {
        List<Long> ids = new ArrayList<>();
        try {
            SearchResponse<Map> response = elasticsearchClient.search(s -> s
                            .index(INDEX_NAME)
                            .query(q -> q
                                    .bool(b -> {
                                        b.must(m -> m
                                                .multiMatch(mm -> mm
                                                        .query(keyword)
                                                        .fields("name", "content")
                                                )
                                        );
                                        if (libraryId != null) {
                                            b.filter(f -> f
                                                    .term(t -> t
                                                            .field("libraryId")
                                                            .value(libraryId)
                                                    )
                                            );
                                        }
                                        return b;
                                    })
                            )
                            .size(100)
                    , Map.class);

            for (Hit<Map> hit : response.hits().hits()) {
                Map source = hit.source();
                if (source != null && source.get("id") != null) {
                    ids.add(((Number) source.get("id")).longValue());
                }
            }
        } catch (Exception e) {
            log.warn("Failed to search documents", e);
        }
        return ids;
    }

    public void rebuildIndex() {
        try {
            if (elasticsearchClient.indices().exists(e -> e.index(INDEX_NAME)).value()) {
                elasticsearchClient.indices().delete(d -> d.index(INDEX_NAME));
            }
            init();
            
            List<Document> documents = documentMapper.selectList(null);
            for (Document doc : documents) {
                indexDocument(doc);
            }
            log.info("Rebuilt index with {} documents", documents.size());
        } catch (Exception e) {
            log.warn("Failed to rebuild index", e);
        }
    }
}
