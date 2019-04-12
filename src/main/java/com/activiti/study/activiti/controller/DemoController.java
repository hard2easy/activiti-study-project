package com.activiti.study.activiti.controller;

import com.activiti.study.activiti.utils.ActivitiFlowPictureUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.activiti.engine.*;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/activiti")
@Api(value = "activiti controller")
public class DemoController {
    private static final Logger logger = LoggerFactory.getLogger(DemoController.class);
    @Autowired
    private ProcessEngineConfiguration processEngineConfiguration;
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private HistoryService historyService;
    @Autowired
    private ManagementService managementService;
    @Autowired
    private IdentityService identityService;
    @Autowired
    private FormService formService;
    /**
     * 1：启动流程实例
     */
    @ApiOperation(value = "启动流程实例并且完成请假申请", notes = "启动流程实例并且完成请假申请")
    @GetMapping("/testStartProcessInstance")
    public void testStartProcessInstance(@RequestParam("procdefKey") String procdefKey){
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(procdefKey);
        Task vacationApply = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        Map<String,Object> param = new HashMap<>();
        param.put("users","1,2,3,4,5");
        taskService.setAssignee(vacationApply.getId(),"张三");
        taskService.complete(vacationApply.getId(),param);
    }
    /**
     * 3.查询代办任务
     */
    @ApiOperation(value = "查询代办任务", notes = "查询代办任务")
    @GetMapping("/taskToDoQuery")
    public List<Task> queryTaskToDo(String taskAssignee){
        List<Task> tasks = taskService.createTaskQuery().taskCandidateOrAssigned(taskAssignee).list();
        return tasks;
    }
    /**
     * 3.查询已完成任务
     */
    @ApiOperation(value = "查询已完成任务", notes = "查询已完成任务")
    @GetMapping("/taskDidQuery")
    public List<HistoricTaskInstance> queryTaskDidQuery(String taskAssignee){
        List<HistoricTaskInstance> tasks = historyService.createHistoricTaskInstanceQuery().taskAssignee(taskAssignee).list();
        return tasks;
    }
    /**
     * 4.经理完成任务并且指定总经理
     */
    @ApiOperation(value = "经理完成任务并且指定总经理", notes = "经理完成任务并且指定总经理")
    @GetMapping("/managerConfirmTask")
    public void managerConfirmTask(String taskId){
        Map<String,Object> param = new HashMap<>();
        param.put("groupId","1");
        taskService.setAssignee(taskId,"admin");
        taskService.complete(taskId,param); //查看act_ru_task数据表
    }

    /**
     * 5.总经理认领任务
     */
    @ApiOperation(value = "教务处主任完成任务", notes = "教务处主任完成任务")
    @GetMapping("/claimTask")
    public void claimTask(String taskId){
        taskService.claim(taskId,"总经理1");
    }
    /**
     * 0.建立用户以及用户组
     */
    @ApiOperation(value = "建立用户以及用户组", notes = "建立用户以及用户组")
    @GetMapping("/createUserAndGroup")
    public void createUserAndGroup(){
        //项目中每创建一个新用户，对应的要创建一个Activiti用户
        //两者的userId和userName一致
        User admin=identityService.newUser("admin");
        admin.setLastName("admin");
        identityService.saveUser(admin);
        //项目中每创建一个角色，对应的要创建一个Activiti用户组
        Group adminGroup=identityService.newGroup("1");
        adminGroup.setName("admin");
        identityService.saveGroup(adminGroup);
        //用户与用户组关系绑定
        identityService.createMembership("admin","1");
    }
    /**
     * 6.查询组用户
     */
    @ApiOperation(value = "查询组用户", notes = "查询组用户")
    @GetMapping("/getUserByGroupId")
    public List<User> getUserByGroupId(String groupId){
        List<User> users = identityService.createUserQuery().memberOfGroup(groupId).list();
        return users;
    }
    /**
     * 7.查看流程图
     */
    @ApiOperation(value = "查看流程图", notes = "查看流程图")
    @RequestMapping(value = "/getFlowImg/{processInstanceId}")
    public void getFlowImgByInstantId(@PathVariable("processInstanceId") String processInstanceId, HttpServletResponse response) {
        try {
            System.out.println("processInstanceId:" + processInstanceId);
            ActivitiFlowPictureUtil.getFlowImgByInstanceId(processInstanceId, response.getOutputStream(),historyService,processEngineConfiguration,repositoryService);
        } catch (IOException e) {
            logger.error("查看流程图失败:" + e.getMessage(),e);
        }
    }
}
