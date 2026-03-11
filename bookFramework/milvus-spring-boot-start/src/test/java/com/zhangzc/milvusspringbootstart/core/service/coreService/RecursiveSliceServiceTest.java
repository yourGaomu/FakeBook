package com.zhangzc.milvusspringbootstart.core.service.coreService;

import com.zhangzc.milvusspringbootstart.config.property.EmbeddingProperty;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RecursiveSliceServiceTest {

    @Test
    void testSlice() {
        EmbeddingProperty property = mock(EmbeddingProperty.class);
        when(property.getSliceOverlap()).thenReturn(5);

        RecursiveSliceService service = new RecursiveSliceService(property);
        String text = "This is a long text that needs to be sliced recursively using langchain4j splitter.";
        
        // size = 20, overlap = 5
        List<String> slices = service.slice(text, 20);
        
        Assertions.assertFalse(slices.isEmpty(), "Slices should not be empty");
        
        for (String slice : slices) {
            System.out.println("Slice: [" + slice + "] length: " + slice.length());
            Assertions.assertTrue(slice.length() <= 20, "Slice length should be <= 20");
        }
    }
}
