package gwl.service;

import gwl.pojo.DTO.TimelineDTO;

public interface TimelineService {
    /**
     * 推送帖子
     */
    void postTimeline(TimelineDTO TimelineDTO);
}


