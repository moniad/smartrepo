package pl.edu.agh.smart_repo.services.index;

import com.alibaba.fastjson.JSON;
import io.vavr.control.Option;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.WildcardQueryBuilder;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.RestClients;
import org.springframework.stereotype.Service;
import pl.edu.agh.smart_repo.common.document_fields.DocumentField;
import pl.edu.agh.smart_repo.common.document_fields.DocumentStructure;
import pl.edu.agh.smart_repo.common.file.FileInfo;
import pl.edu.agh.smart_repo.common.response.Result;
import pl.edu.agh.smart_repo.common.response.ResultType;
import pl.edu.agh.smart_repo.configuration.ConfigurationFactory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static pl.edu.agh.smart_repo.common.document_fields.DocumentField.CONTENTS;
import static pl.edu.agh.smart_repo.common.document_fields.DocumentField.PATH;

@Slf4j
@Service
public class IndexerService {
    private final String indexWithHost;
    private final String indexName;
    private final RestHighLevelClient restHighLevelClient;

    @Autowired
    public IndexerService(ConfigurationFactory configurationFactory,
                          @Value("${elastic.index.number_of_shards}") int number_of_shards,
                          @Value("${elastic.index.number_of_replicas}") int number_of_replicas) {
        log.info("Init indexer service");

        ClientConfiguration clientConfiguration = ClientConfiguration.builder()
                .connectedTo("elasticsearch:9200")
                .build();
        restHighLevelClient = RestClients.create(clientConfiguration)
                .rest();

        indexName = configurationFactory.getIndex();
        indexWithHost = configurationFactory.getElasticSearchAddress() + "/" + configurationFactory.getIndex();

        createInitIndexAndMappingRequest(number_of_shards, number_of_replicas);
    }

    public Result indexDocument(DocumentStructure documentStructure) {
        IndexRequest indexDocumentRequest = createIndexDocumentRequest(documentStructure);
        log.info("Send index document: '" + indexDocumentRequest + "'");
        try {
            IndexResponse indexResponse = restHighLevelClient.index(indexDocumentRequest, RequestOptions.DEFAULT);
            log.info("Index document response: '" + indexResponse + "'");
        } catch (IOException e) {
            log.error("Unexpected error while connecting to ElasticSearch (index document mappings)");
            return new Result(ResultType.FATAL_FAILURE);
        } catch (ElasticsearchStatusException e) {
            log.error("Cannot index document. Message: " + e.getMessage());
            return new Result(ResultType.FATAL_FAILURE);
        }

        return new Result(ResultType.SUCCESS);
    }

    public Result deleteFileFromIndex(DocumentStructure document) {
        DeleteByQueryRequest deleteFileFromIndexRequest = createDeleteFileFromIndexRequest(document);
        log.info("Delete file request: '" + deleteFileFromIndexRequest.getSearchRequest().source().toString() + "'");
        String failureMessage = String.format("Cannot delete file %s from index", document.getName());
        try {
            BulkByScrollResponse deleteResponse = restHighLevelClient.deleteByQuery(deleteFileFromIndexRequest, RequestOptions.DEFAULT);
            log.info("Index response: " + deleteResponse);
        } catch (IOException e) {
            log.error("Unexpected error while connecting to ElasticSearch (delete from index)");
            return new Result(ResultType.FATAL_FAILURE, failureMessage);
        } catch (ElasticsearchStatusException e) {
            log.error("Cannot delete file from index. Message: " + e.getMessage());
            return new Result(ResultType.FATAL_FAILURE, failureMessage);
        }

        return new Result(ResultType.SUCCESS, String.format("File %s deleted successfully", document.getName()));
    }

    public Option<List<FileInfo>> search(String phrase) {
        Option<List<FileInfo>> foundFiles = Option.none();

        try {
            SearchRequest searchRequest = new SearchRequest(indexName);
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.query(QueryBuilders.matchPhraseQuery(CONTENTS.toString(), phrase));
            searchRequest.source(searchSourceBuilder);

            log.info("Search query: '" + searchRequest.source().toString() + "'");

            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            log.info("Search response: [");

            SearchHit[] searchHits = searchResponse.getHits().getHits();
            List<FileInfo> results = Arrays.stream(searchHits)
                    .map(hit -> JSON.parseObject(hit.getSourceAsString(), DocumentStructure.class))
                    .peek(documentStructure -> log.info("Search hit: '" + Paths.get(documentStructure.getPath(), documentStructure.getName()).toString() + "'"))
                    .map(FileInfo::of)
                    .collect(Collectors.toList());
            log.info("]");
            return Option.of(results);
        } catch (IOException e) {
            log.error("Error while searching index for phrase: " + phrase);
        } catch (ElasticsearchStatusException e) {
            log.error("Cannot search document. Message: " + e.getMessage());
        }
        return foundFiles;
    }

    private void createInitIndexAndMappingRequest(int numberOfShards, int numberOfReplicas) {
        CreateIndexRequest request = createIndexRequest(numberOfShards, numberOfReplicas);
        XContentBuilder builder = createMappingRequest();
        request.mapping(builder);

        log.info("Send init index: '" + request + "', address: " + indexWithHost);

        try {
            CreateIndexResponse createIndexResponse = restHighLevelClient.indices().create(request, RequestOptions.DEFAULT);
            log.info("Init index response: '" + createIndexResponse + "'");
        } catch (ElasticsearchException e) {
            log.error("Error while setting up index (index already exists)");
        } catch (IOException e) {
            log.error("Unexpected error while connecting to ElasticSearch (init index)");
        }
    }

    private CreateIndexRequest createIndexRequest(int numberOfShards, int numberOfReplicas) {
        CreateIndexRequest createIndexRequest = new CreateIndexRequest(indexName);
        createIndexRequest.settings(Settings.builder()
                .put("index.number_of_shards", numberOfShards)
                .put("index.number_of_replicas", numberOfReplicas)
        );
        return createIndexRequest;
    }

    private XContentBuilder createMappingRequest() {
        XContentBuilder builder = null;
        try {
            builder = XContentFactory.jsonBuilder();
            builder.startObject();
            {
                builder.startObject("properties");
                {
                    for (DocumentField documentField : DocumentField.values()) {
                        builder.startObject(documentField.toString());
                        {
                            if (documentField == PATH)
                                builder.field("type", "keyword");
                            else
                                builder.field("type", "text");
                        }
                        builder.endObject();
                    }
                }
                builder.endObject();
            }
            builder.endObject();
        } catch (IOException e) {
            log.error("Error while creating index mapping request");
        }
        return builder;
    }

    private IndexRequest createIndexDocumentRequest(DocumentStructure documentStructure) {
        Map<String, Object> jsonMap = new HashMap<>();
        for (DocumentField documentField : DocumentField.values()) {
            jsonMap.put(documentField.toString(), documentStructure.getByDocumentField(documentField));
        }
        return new IndexRequest(indexName).source(jsonMap);
    }

    private DeleteByQueryRequest createDeleteFileFromIndexRequest(DocumentStructure documentStructure) {
        DeleteByQueryRequest request = new DeleteByQueryRequest(indexName);
        request.setQuery(new WildcardQueryBuilder(PATH.toString(), documentStructure.getPath() + "*"));
        return request;
    }
}
