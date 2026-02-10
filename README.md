# fernflower

这个repo是jetbrains fernflower的fork版本，原仓库是基于java21，这个仓库将语法退到java8，这样得到的jar包可以在java8+的任意环境运行，适用性更广。

虽然项目基于java8工具链，但是最终的反编译器，可以对`java4-22`的字节码进行反编译。

# cli usage

与原项目一样，你可以使用release中的jar包，直接在命令行使用，对项目进行反编译
```bash
java -jar fernflower.jar [-<option>=<value>]* [<source>]+ <destination>`
```
更多参数可以参考https://github.com/JetBrains/fernflower

# sdk usage

你还可以通过依赖的方式引入的自己的项目(java>=8)中，对内容进行反编译。 引入方式[jitpack](https://jitpack.io/#sunwu51/fernflower)找到最新的release版本即可。

使用`InMemoryDecompiler#decompileClass`可以直接对字节码`byte[]`进行反编译，该函数需要指定多个参数。
- `Map<String, byte[]> classes` 类名字节码映射，因为一个类可能有一些匿名类和内部类，都放到这个map中。
- `String entrypoint` 要反编译的入口类名，一定是map参数中的一个key。
- `Map<String, Object> options` 反编译参数设置，可以为null或[参考](https://github.com/JetBrains/fernflower)进行自行设置。
- `IFernflowerLogger logger`设置为null即可。
- `Function<String, Object> hookForGetInnerClass`在编译过程中去获取其他类的时候的钩子函数，辅助观察的，可以设置为null。
