# 💫 Niagara

Niagara is a lightweight Java library providing bindings to the [Ultralight web renderer](https://ultralig.ht/). It's designed to be easy to use and integrate into Java applications as well as efficient, allowing developers to seamlessly render web content within their applications.

Unlike most native library Java bindings projects, Niagara supports both JNI and FFM as ways of calling into native code. The project requires Java 17 as a minimum, with JNI supported on Java 17 or newer and FFM supported on Java 22 or newer.

---

## 📥 Usage

**Coming Soon!**

## 🏗️ Project Structure

Niagara uses a modular approach that keeps everything cleanly separated and organized. These modules are as listed:

1. `niagara-api` - The public API that you and everyone else use when implementing Ultralight into your apps.
2. `niagara-codegen` - A code generator that produces JNI and FFM bindings for a given annotated native interface.
3. `niagara-core` - The backend abstraction layer implemented per native backend that Niagara supports.
4. `niagara-ffm` - The FFM native backend implementation.
5. `niagara-jni` - The JNI native backend implementation.

## 🛠️ Building From Sources

**Coming Soon!**

## ❤️ Supporting Me

[![kofi-singular-alt](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/compact/donate/kofi-singular-alt_vector.svg)](https://ko-fi.com/ayydxnfv)

I work on Niagara as a solo developer in my spare time. If you'd like to support me as well as this and my other projects, you can donate to me via Ko-fi!

## 📃 License

Niagara is licensed under the free and open-source license, GNU LGPLv3. For more information, please read the [license](https://choosealicense.com/licenses/lgpl-3.0/).
