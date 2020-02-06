[简书地址]((https://www.jianshu.com/p/3f6e7dc06b23)
[GITHUB地址](https://github.com/bigmanLau/ASM_DedounceClick_Android)
[使用demo](https://github.com/bigmanLau/ASM_DedounceClick_Android_Demo)
该项目已开源，可以拉到文章底部查看使用方法。


##### 公司背景：
深圳我们在线教育有限公司
##### 技术背景：
安卓高级开发工程师
##### 需求背景 : 
用户点击课程播放，打开多次课程详情页面，典型的点击抖动问题，但是公司项目成熟稳定，不可能重新为每个点击事件添加固定的代理或者装饰类，工作量太大，所以这里我选择了AOP方案，进行class代码切入，为每个点击事件添加自己的点击拦截代码

#####读懂本篇文章需要：
1.自定义gradle插件和熟悉调试技巧，不懂得可以先移步[自定义gradle插件教程]([https://www.jianshu.com/p/80ac92253112](https://www.jianshu.com/p/80ac92253112)
)
2.ASM基本常识和基本操作，不懂的可以先移步[ASM教程]([https://www.jianshu.com/p/c2c1d350d245](https://www.jianshu.com/p/c2c1d350d245)
)

#####文章开始
---------------------
######1.思路分析
我们通常需要拦截点击事件，无非就是监听上次点击的是哪个视图，点击间隔是否超过我们规定的，如果超过了我们就可以让他进行二次点击，否则我们拦截点击。

我们先看下面两段代码：

>第一个段代码是我们常见的点击事件，这里我没有用匿名内部类，因为我们需要查看它的字节码（`show bytecode outline`插件）

![image.png](https://upload-images.jianshu.io/upload_images/12262980-a803cd4ae2f14599.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
>下面我们看第二段代码截图， 第二段代码就是我们要实现的点击拦截代码的插入，这里的这个`CheckClick`在github上可以看看我的代码，当然你也可以自己去实现

![image.png](https://upload-images.jianshu.io/upload_images/12262980-9f3c9a99d0a70097.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

######2.ASM插入逻辑
上面我们了解到了检测点击的过程，同时我们拿到了两次代码的差异代码
差异代码如下：
````
            mv.visitVarInsn(ALOAD, 1);
            mv.visitMethodInsn(INVOKEVIRTUAL, "android/view/View", "getId", "()I", false);
            mv.visitMethodInsn(INVOKESTATIC, "com/example/debounceCheck/CheckClick", "checkIsClicked", "(I)Z", false);
            Label l1 = new Label();
            mv.visitJumpInsn(IFEQ, l1);
            Label l2 = new Label();
            mv.visitLabel(l2);
            mv.visitLineNumber(10, l2);
            mv.visitInsn(RETURN);
            mv.visitLabel(l1);
            mv.visitLineNumber(12, l1);
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
````
现在我们要做的就是利用gradle插件的`tramsform任务`把代码插入到点击事件里面
1.其实关键就在两个地方，一个这ClassVisitor里面你需要过滤点击事件，因为类里面有各种各样的方法，但是我们只需要插入点击事件的方法就行，具体代码如下
````
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
````
关键在这几行
````
 if(Utils.isViewOnclickMethod(access,name,desc)){
            System.out.println("OnClickClassVisitor : change method ---->"+name);
            return new OnClickMethodVisitor(mv,this.project);
        }else if(Utils.isListViewOnItemOnclickMethod(access,name,desc)){
            System.out.println("OnClickClassVisitor : change method ---->"+name);
            return  new OnClickMethodVisitor(mv,this.project);
        }
````
这里判断了两个点击类型，一个是普通的view点击事件一个是列表点击事件，具体代码如下
````
 static boolean isViewOnclickMethod(int access, String name, String desc) {
    return (Utils.isPublic(access) && !Utils.isStatic(access) && !isAbstract(access)) //
        && name.equals("onClick") //
        && desc.equals("(Landroid/view/View;)V");
  }

  static boolean isListViewOnItemOnclickMethod(int access, String name, String desc) {
    return (Utils.isPublic(access) && !Utils.isStatic(access) && !isAbstract(access)) && //
        name.equals("onItemClick") && //
        desc.equals("(Landroid/widget/AdapterView;Landroid/view/View;IJ)V");
  }
````
这些都比较好理解
2.第二个地方我们过滤完就需要在这个方法里面插入代码了，所以我们需要改写一下
````
public class OnClickMethodVisitor extends MethodVisitor {
    private  Project project;
    public OnClickMethodVisitor(MethodVisitor mv, Project project) {
        super(Opcodes.ASM6,mv);
        this.project=project;
    }
    @Override
    public void visitCode() {
        super.visitCode();
            mv.visitVarInsn(ALOAD, 1);
            mv.visitMethodInsn(INVOKEVIRTUAL, "android/view/View", "getId", "()I", false);
            mv.visitMethodInsn(INVOKESTATIC, "com/example/debounceCheck/CheckClick", "checkIsClicked", "(I)Z", false);
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
````
这样我们就基本完成了
我们配置了本地的上传，所以点击编辑器右侧的gradle找到uploadArchives发布代码就能使用
![image.png](https://upload-images.jianshu.io/upload_images/12262980-3f2f4f55fa07f49d.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

######3.本地使用方法
1.在项目的build.gradle配置如下
![image.png](https://upload-images.jianshu.io/upload_images/12262980-827a406a982156e6.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
2.在app模块的build.gradle配置如下
![image.png](https://upload-images.jianshu.io/upload_images/12262980-2c35c08aabd9bb3c.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
第一个参数是你自己自定义的点击拦截类 第二个是指定要处理的包名 节省时间


######4.运行效果图
![image.png](https://upload-images.jianshu.io/upload_images/12262980-3864b2ecb6ecc0de.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

![image.png](https://upload-images.jianshu.io/upload_images/12262980-514802220ab95142.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

######5.使用方法

### 项目个目录的build.gradle引入下面代码

```
buildscript {
  repositories {
    maven {
      url "https://plugins.gradle.org/m2/"
    }
  }
  dependencies {
    classpath "gradle.plugin.com.bigman:testgradle:1.0.2"
  }
}
```
然后在你要使用的模块比如app模块使用此插件
```
apply plugin: "com.bigman.clickInject"
OnClickExtension{
    checkClass ="com/example/debounceCheck/CheckClick"
}
```
这里的`com/example/debounceCheck/CheckClick`就是你自定义的检测点击的方法，可以直接参考demo里的代码







