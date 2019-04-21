package com.activiti.study.activiti.delegate;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;

public class DealErrorDelegate implements JavaDelegate{
    @Override
    public void execute(DelegateExecution delegateExecution) {
        System.out.println("主流程报错,进入子流程");
    }
}
