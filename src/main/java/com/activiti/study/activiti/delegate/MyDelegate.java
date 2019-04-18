package com.activiti.study.activiti.delegate;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;

public class MyDelegate implements JavaDelegate{
    @Override
    public void execute(DelegateExecution delegateExecution) {
        System.out.println("-------asycJob");
    }
}
