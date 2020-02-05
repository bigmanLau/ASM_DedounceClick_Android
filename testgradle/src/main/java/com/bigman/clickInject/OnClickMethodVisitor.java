package com.bigman.clickInject;

import org.apache.http.util.TextUtils;
import org.gradle.api.Project;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import static org.objectweb.asm.Opcodes.*;

public class OnClickMethodVisitor extends MethodVisitor {
    private  Project project;
    public OnClickMethodVisitor(MethodVisitor mv, Project project) {
        super(Opcodes.ASM6,mv);
        this.project=project;
    }
    @Override
    public void visitCode() {
        super.visitCode();

        OnClickExtension extension = (OnClickExtension) this.project.getExtensions().findByName("OnClickExtension");
        if(!TextUtils.isEmpty(extension.checkClass)){
            System.out.println("[BigmanClickPlugin]"+extension.checkClass.equals("com/example/debounceCheck/CheckClick"));
            mv.visitVarInsn(ALOAD, 1);
            mv.visitMethodInsn(INVOKEVIRTUAL, "android/view/View", "getId", "()I", false);
            mv.visitMethodInsn(INVOKESTATIC, extension.checkClass, "checkIsClicked", "(I)Z", false);
            Label l1 = new Label();
            mv.visitJumpInsn(IFEQ, l1);
            Label l2 = new Label();
            mv.visitLabel(l2);
            mv.visitLineNumber(10, l2);
            mv.visitInsn(RETURN);
            mv.visitLabel(l1);
            mv.visitLineNumber(12, l1);
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
        }

    }
}
