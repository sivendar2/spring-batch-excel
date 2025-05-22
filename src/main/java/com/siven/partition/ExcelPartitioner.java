
package com.siven.partition;

import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.*;

public class ExcelPartitioner implements Partitioner {

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        try (InputStream is = new ClassPathResource("employees.xlsx").getInputStream()) {
            Workbook workbook = WorkbookFactory.create(is);
            Sheet sheet = workbook.getSheetAt(0);
            int totalRows = sheet.getPhysicalNumberOfRows() - 1; // skip header

            int rowsPerPartition = totalRows / gridSize;
            int remainder = totalRows % gridSize;

            Map<String, ExecutionContext> partitions = new HashMap<>();

            int start = 1; // row 0 is header
            for (int i = 0; i < gridSize; i++) {
                int end = start + rowsPerPartition - 1;
                if (i == gridSize - 1) end += remainder;

                ExecutionContext context = new ExecutionContext();
                context.putInt("startRow", start);
                context.putInt("endRow", end);

                partitions.put("partition" + i, context);
                start = end + 1;
            }

            return partitions;
        } catch (Exception e) {
            throw new RuntimeException("Partitioning failed", e);
        }
    }
}

