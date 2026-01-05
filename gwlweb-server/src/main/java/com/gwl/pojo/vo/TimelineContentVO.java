package com.gwl.pojo.vo;

import java.util.List;



import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TimelineContentVO {
    String context;
    String createdAt;
    List<String> imgUrls;
}
