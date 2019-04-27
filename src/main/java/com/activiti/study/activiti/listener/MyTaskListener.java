package com.activiti.study.activiti.listener;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.impl.el.JuelExpression;

public class MyTaskListener implements TaskListener{
    private JuelExpression name;
    @Override
    public void notify(DelegateTask delegateTask) {
        System.out.println("属性注入:" + name.getExpressionText());
    }
}
