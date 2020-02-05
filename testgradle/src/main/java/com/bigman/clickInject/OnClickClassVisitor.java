package com.bigman.clickInject;

import org.gradle.api.Project;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class OnClickClassVisitor extends ClassVisitor implements Opcodes {
    private String mClassName;
    private Project project;

    public OnClickClassVisitor(ClassVisitor  mv, Project project) {
        super(Opcodes.ASM6,mv);
        this.project=project;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        System.out.println("OnClickClassVisitor:visit----->started:"+name);
        this.mClassName=name;
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        System.out.println("OnClickClassVisitor:visitMethod :"+name);
        MethodVisitor mv=cv.visitMethod(access,name,desc,signature,exceptions);
        System.out.println("OnClickClassVisitor:visitMethod ---------->started:"+this.mClassName);
        if(Utils.isViewOnclickMethod(access,name,desc)){
            System.out.println("OnClickClassVisitor : change method ---->"+name);
            return new OnClickMethodVisitor(mv,this.project);
        }else if(Utils.isListViewOnItemOnclickMethod(access,name,desc)){
            System.out.println("OnClickClassVisitor : change method ---->"+name);
            return  new OnClickMethodVisitor(mv,this.project);
        }
        return mv;
    }
}

