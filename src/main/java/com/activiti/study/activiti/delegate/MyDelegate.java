package com.activiti.study.activiti.delegate;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.activiti.engine.impl.el.JuelExpression;

/**
 * 异步任务处理器
 */
public class MyDelegate implements JavaDelegate{
    private JuelExpression name;
    @Override
    public void execute(DelegateExecution delegateExecution) {
        System.out.println("进入自定义处理");
    }
}
