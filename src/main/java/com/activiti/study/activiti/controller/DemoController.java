package com.activiti.study.activiti.controller;

import com.activiti.study.activiti.entity.Person;
import com.activiti.study.activiti.utils.ActivitiFlowPictureUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.activiti.engine.*;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
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
     *        流程定义启动就会有流程实例以及执行对象的概念,如果流程执行过程中同时有多少条分支那么执行对象表中就只会有多少个
     *        子执行流,不管什么情况都一定还会有一条根执行流.
     */
    @ApiOperation(value = "启动流程实例并且完成请假申请", notes = "启动流程实例并且完成请假申请")
    @GetMapping("/testStartProcessInstance")
    public void testStartProcessInstance(@RequestParam("procdefKey") String procdefKey,String vacationDay){
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(procdefKey);
        Task vacationApply = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        Map<String,String> param = new HashMap<>();
        param.put("reason","reason原因");
        param.put("users","1,2,3,4,5");
        param.put("vacationDay",vacationDay);
//        taskService.setVariable(vacationApply.getId(),"users","1,2,3,4,5");
        taskService.setAssignee(vacationApply.getId(),"张三");
        formService.submitTaskFormData(vacationApply.getId(),param);
    }
    /**
     * 3.查询代办任务
     */
    @ApiOperation(value = "查询代办任务", notes = "查询代办任务")
    @GetMapping("/taskToDoQuery")
    public List<Task> queryTaskToDo(String userId){
        //查询代办个人任务
        List<Task> tasks = taskService.createTaskQuery().taskAssignee(userId).list();
        //查询代办组任务
        List<Task> groupTasks = taskService.createTaskQuery().taskCandidateUser(userId).list();
        //查询所有代办组任务
        List<Task> allTasks = taskService.createTaskQuery().taskCandidateOrAssigned(userId).list();
        return allTasks;
    }
    /**
     * 3.查询已完成任务
     */
    @ApiOperation(value = "查询已完成任务", notes = "查询已完成任务")
    @GetMapping("/taskDidQuery")
    public List<HistoricTaskInstance> queryTaskDidQuery(String userId){
        List<HistoricTaskInstance> tasks = historyService.createHistoricTaskInstanceQuery().taskAssignee(userId).list();
        return tasks;
    }
    /**
     * 4.经理完成任务并且指定总经理
     */
    @ApiOperation(value = "经理完成任务并且指定总经理", notes = "经理完成任务并且指定总经理")
    @GetMapping("/managerConfirmTask")
    public void managerConfirmTask(String taskId,String groupId){
        Map<String,Object> param = new HashMap<>();
        param.put("groupId",groupId);
        taskService.setAssignee(taskId,"manager");
        taskService.complete(taskId,param); //查看act_ru_task数据表
    }

    /**
     * 5.认领任务
     */
    @ApiOperation(value = "认领任务", notes = "认领任务")
    @GetMapping("/claimTask")
    public void claimTask(String taskId,String userId){
        taskService.claim(taskId,userId);
    }
    /**
     * 0.建立用户以及用户组
     */
    @ApiOperation(value = "建立用户以及用户组", notes = "建立用户以及用户组")
    @GetMapping("/createUserAndGroup")
    public void createUserAndGroup(){
        //项目中每创建一个新用户，对应的要创建一个Activiti用户
        //两者的userId和userName一致
        User admin=identityService.newUser("user1");
        admin.setLastName("topManager");
        identityService.saveUser(admin);
        //项目中每创建一个角色，对应的要创建一个Activiti用户组
        Group adminGroup=identityService.newGroup("5");
        adminGroup.setName("topManagerGroup");
        identityService.saveGroup(adminGroup);
        //用户与用户组关系绑定
        identityService.createMembership("user1","5");
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
    @GetMapping(value = "/getFlowImg/{processInstanceId}")
    public void getFlowImgByInstantId(@PathVariable("processInstanceId") String processInstanceId, HttpServletResponse response) {
        try {
            System.out.println("processInstanceId:" + processInstanceId);
            ActivitiFlowPictureUtil.getFlowImgByInstanceId(processInstanceId, response.getOutputStream(),historyService,processEngineConfiguration,repositoryService);
        } catch (IOException e) {
            logger.error("查看流程图失败:" + e.getMessage(),e);
        }
    }
    /**
     * 8.任务委托
     */
    @ApiOperation(value = "任务委托", notes = "任务委托")
    @GetMapping(value = "/deputeTask")
    public void deputeTask(String taskId,String userId) {
        String owner = taskService.createTaskQuery().taskId(taskId).singleResult().getAssignee();
//        taskService.setOwner(taskId,owner);
        taskService.delegateTask(taskId,userId);
    }
    /**
     * 9.完成任务委托
     */
    @ApiOperation(value = "完成委托任务", notes = "完成委托任务")
    @GetMapping(value = "/finishDeputeTask")
    public void finishDeputeTask(String taskId) {
        taskService.resolveTask(taskId);
    }
    /**
     * 10.查询待完成委托任务
     */
    @ApiOperation(value = "查询待处理的委托任务", notes = "查询待处理的委托任务")
    @GetMapping(value = "/queryToFinishDeputeTask")
    public List<Task> queryToFinishDeputeTask(String taskAssignId) {
        //可以按照表的每个字段进行查询,如果通过.字段名查询不到可以通过类似"task"(可以适配其它组件)+字段名查询  -----方法名的问题
//      taskService.createTaskQuery().taskName().executionId()
//      原生sql查询  变量使用 #{}进行引用
//      taskService.createNativeTaskQuery().sql("sql语句").parameter().list();
//      List<Task> tasksDefine = taskService.createNativeTaskQuery().sql("select * from act_ru_task where OWNER_ = #{taskAssignId}")
//                .parameter("taskAssignId", taskAssignId).list();
        List<Task> tasks = taskService.createTaskQuery().taskOwner(taskAssignId).list();
        return tasks;
    }
    /**
     * 11.部署流程定义   流程图会自动生成
     *   流程定义进行部署的时候会校验流程文件的正确性,如果不正确部署时会报错,可以通过
     *   repositoryService.createDeployment().disableSchemaValidation() 关闭验证
     *   如果部署的时候未提供流程图,会自动生成一份流程图  命名规则为:   bpmn的文件名.流程文件的id.png
     *
     *   实际生产中可能会针对专门部门设计专门的流程,此时在部署流程定义的时候可以配置该流程定义只能被指定的用户/用户组进行使用
     *   repositoryService.addCandidateStarterUser(flowDefineId,userId)
     *
     *   可以通过如下方法查询出用户可以使用哪些流程定义:
     *   repositoryService.createProcessDefinitionQuery().startableByUser(userId).list();
     */
    @ApiOperation(value = "部署流程定义", notes = "部署流程定义")
    @GetMapping(value = "/deployFlowDefine")
    public void deployFlowDefine(String userId) {
//      repositoryService.createDeployment().addBpmnModel("",bpmnModel).deploy()  bpmnModel可用通过代码绘制流程图,特定类表示图中的连线、任务等模型  用于动态产生流程图
        Deployment deployment = repositoryService.createDeployment().name("部署流程定义").addClasspathResource("processes/delopment.bpmn").deploy();
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().deploymentId(deployment.getId()).singleResult();
        repositoryService.addCandidateStarterUser(processDefinition.getId(),userId);
    }
    /**
     * 12.删除部署数据(部署信息、流程定义、二进制文件表都会删除)
     *    true: 级联删除,如果存在正在执行的流程实例,那么必须设置为级联删除否则删除的时候会报错
     */
    @ApiOperation(value = "删除部署数据", notes = "部署流删除部署数据程定义")
    @GetMapping(value = "/deleteDeployData")
    public void deleteDeployData(String deployId) {
        repositoryService.deleteDeployment(deployId,true);
    }
    /**
     * 13.终止/激活流程定义     终止后不允许再启动
     */
    @ApiOperation(value = "终止/激活流程定义", notes = "终止/激活流程定义")
    @GetMapping(value = "/operatorFlowDefine")
    public void operatorFlowDefine(String flowDefineId) {
        repositoryService.suspendProcessDefinitionById(flowDefineId);
        repositoryService.activateProcessDefinitionById(flowDefineId);
    }
    /**
     * 14.设置变量
     *       由于在act_ru_task以及act_hi_taskinst都会存在此时待执行的任务数据,因此设置变量时在历史变量表以及运行时变量表都会有一条
     *       变量信息,如果变量设置的是序列化的变量,那么在资源文件表中会存在两条记录,文件内容(变量内容)自然是一样的
     *    局部变量与全局变量
     *       当局部变量和流程变量存在于同一个任务节点则查询时得到的是最后赋予该key的值，但是当该任务节点完成后，下一个节点获取的流程变量仍然是最初的流程变量的值，而不是局部变量的值
     *    通过runtimeService设置的局部变量绑定的是执行对象
     */
    @ApiOperation(value = "设置变量", notes = "设置变量")
    @GetMapping(value = "/setVariable")
    public void setVariable(String taskId,String processInstanceId) {
        taskService.setVariable(taskId,"var1","1");  //该任务所属流程实例中的所有任务都可以取到值
        taskService.setVariableLocal(taskId,"var2","var2");//仅该任务可以获取
        Person p = new Person();
        p.setName("name");
        p.setAddress("address");
        taskService.setVariable(taskId,"person",p);  //存入act_ru_variable(BYTEARRAY_ID_ 对应资源文件表) 具体内容在act_ge_bytearray表
        //添加流程附件   内容存到资源文件表中,其他数据存储在附件表中
        taskService.createAttachment("doc",taskId,processInstanceId,"附件名字","附件描述",new ByteArrayInputStream("附件流".getBytes()));
    }

    /**
     * 完成  receiveTask   该task只能通过调用trigger方法使得流程继续下去
     * @param executeId
     */
    @ApiOperation(value = "完成receiveTask", notes = "完成receiveTask")
    @GetMapping(value = "/receiveTaskFinish")
    public void receiveTaskFinish() {
        ProcessInstance receiveTaskInstance = runtimeService.startProcessInstanceByKey("receiveTask");
        //查询流程实例id为1的子执行对象    根执行对象只是判断两个执行对象对应的是不是一个流程实例
        Execution execution = runtimeService.createExecutionQuery().processInstanceId(receiveTaskInstance.getId()).onlyChildExecutions().singleResult();
        runtimeService.trigger(execution.getId()); //绑定的是子执行对象的id
    }
    /**
     * catchingEvent  捕获事件(流程一旦碰到捕获事件,流程就进行等待)
     *      信号事件(广播)    发出某一信号后(绘制bpmn图时,会配置需要捕获什么事件),会将所有流程卡在等待该信号的流程继续往下走
     *      消息事件(订阅)    发出某一消息事件((绘制bpmn图时,会配置需要捕获什么事件))绑定执行对象id,只会让指定执行对象继续往下执行
     *      时间事件
     * throwingEvent  抛出事件
     */
    @ApiOperation(value = "catchingEvent", notes = "catchingEvent")
    @GetMapping(value = "/catchingEvent")
    public void signalEvent() {
        ProcessInstance signalEvent = runtimeService.startProcessInstanceByKey("signalEvent");
        Execution execution = runtimeService.createExecutionQuery().processInstanceId(signalEvent.getId()).onlyChildExecutions().singleResult();
        System.out.println(execution.getActivityId());
        runtimeService.signalEventReceived("testSignal");
        runtimeService.messageEventReceived("testSignal",execution.getId());
    }

    /**
     * activit表中涉及到job的表
     *    异步任务:act_ru_job
     *    定时任务:act_ru_timer_job
     *    定时任务暂停:act_ru_suspended_job
     *    定时任务无法执行:act_ru_deadletter_job
     */
    @ApiOperation(value = "dealJob", notes = "dealJob")
    @GetMapping(value = "/dealJob")
    public void dealJob() {

    }

    /**
     * 异步任务需要打开异步执行器的开关 配置在application.yml中
     *   async-executor-activate
     */
    @ApiOperation(value = "异步任务",notes = "异步任务")
    @GetMapping(value = "/asycJob")
    public void asycJob(){
        ProcessInstance asycJobInstance = runtimeService.startProcessInstanceByKey("asycJob");
        System.out.println(asycJobInstance.getActivityId());
    }

    /**
     * 定时任务设置定时的时候可以通过三种形式
     *      Date       详细的时间  2019-04-19T14:00:00
     *      Duration   PT5M   5分钟后触发
     *      cycle      支持cron表达式
     * @throws Exception
     */
    @ApiOperation(value = "定时任务",notes = "定时任务")
    @GetMapping(value = "/timeJob")
    public void timeJob() throws Exception{
        ProcessInstance asycJobInstance = runtimeService.startProcessInstanceByKey("timeJob");
        System.out.println(asycJobInstance.getActivityId());
        //此时就是暂停执行任务
        Thread.sleep(10000);
        runtimeService.suspendProcessInstanceById(asycJobInstance.getId());
        //重新改为待执行定时任务
        Thread.sleep(10000);
        runtimeService.activateProcessInstanceById(asycJobInstance.getId());
    }

    /**
     * 无法执行任务,任务相关查询可以使用如下方法
     *   managementService.createJobQuery();                act_ru_job
     *   managementService.createDeadLetterJobQuery();      act_ru_deadletter_job
     *   managementService.createSuspendedJobQuery();       act_ru_suspended_job
     *   managementService.createTimerJobQuery();           act_ru_timer_job
     * @throws Exception
     */
    @ApiOperation(value = "无法执行工作",notes = "无法执行工作")
    @GetMapping(value = "/noRunTask")
    public void noRunTask() throws Exception{
        ProcessInstance asycJobInstance = runtimeService.startProcessInstanceByKey("noRunTask");
        Job job = managementService.createJobQuery().processInstanceId(asycJobInstance.getId()).singleResult();
        //执行job的时候默认三次执行失败就会记录到无法执行任务记录表中  通过该方法可以设置次数
        managementService.setJobRetries(job.getId(),1);
    }
}
