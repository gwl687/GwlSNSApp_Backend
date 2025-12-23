package gwl.controller.group;

import javax.swing.text.StyledEditorKit.BoldAction;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import gwl.context.BaseContext;
import gwl.pojo.DTO.CreateGroupChatDTO;
import gwl.pojo.VO.GroupChatVO;
import gwl.result.Result;
import gwl.service.UserService;
import gwl.service.group.GroupService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/group")
public class GroupController {
    @Autowired
    GroupService groupService;

    /**
     * 添加群成员
     * 
     * @param groupId
     * @param createGroupChatDTO
     * @return
     */
    @PostMapping(path = "/addgroupmembers/{groupId}", produces = "application/json")
    @Operation(summary = "添加群成员")
    Result<Boolean> addGroupMembers(@PathVariable Long groupId,
            @org.springframework.web.bind.annotation.RequestBody CreateGroupChatDTO createGroupChatDTO) {
        log.info("添加群成员：{}", createGroupChatDTO);
        groupService.addGroupMembers(groupId, createGroupChatDTO);
        return Result.success(true);
    }

    /**
     * 移除群成员
     * 
     * @param groupId
     * @param createGroupChatDTO
     * @return
     */
    @PostMapping(path = "/removegroupmembers/{groupId}", produces = "application/json")
    @Operation(summary = "移除群成员")
    Result<Boolean> removeGroupMembers(@PathVariable Long groupId,
            @org.springframework.web.bind.annotation.RequestBody CreateGroupChatDTO createGroupChatDTO) {
        log.info("移除群成员：{}", createGroupChatDTO);
        groupService.removeGroupMembers(groupId, createGroupChatDTO);
        return Result.success(true);
    }

    @GetMapping(path = "/getlivekittoken/{groupId}", produces = "application/json")
    @Operation(summary = "获取livekittoken")
    Result<String> getLiveKitToken(@PathVariable Long groupId) {
        String token = groupService.createLivekitToken(groupId.toString());
        return Result.success(token);
    }
}
