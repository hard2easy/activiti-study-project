package com.activiti.study.activiti.delegate;

import org.activiti.engine.delegate.BpmnError;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;

public class ThrowErrorBoundaryEventDelegate implements JavaDelegate{
    @Override
    public void execute(DelegateExecution delegateExecution) {
        System.out.println("报错,进入子流程:");
        throw new BpmnError("errorBoundaryEvent");
    }
}
