package com.james.crashhunter.interceptor

object InterceptorManager {
    private var head: Interceptor? = null
    private var tail: Interceptor? = null;

    fun intercept(data: CaptureData): InterceptorState = head?.intercept(data) ?: InterceptorState.NO

    fun addInterceptor(interceptor: Interceptor) {
        if (head == null) {
            head = interceptor
            tail = interceptor
        } else {
            tail?.nextInterceptor = interceptor
            tail = interceptor
        }
    }

    fun remove(interceptor: Interceptor) {
        var cur: Interceptor? = head
        var pre: Interceptor? = null
        while (cur != interceptor) {
            pre = cur
            cur = cur?.nextInterceptor
        }
        if (cur != null) {
            if (pre != null) {
                pre?.nextInterceptor = cur?.nextInterceptor
            } else {
                head = head?.nextInterceptor
            }
        }
        findTail()
    }

    fun interceptorList() : List<Interceptor> {
        var cur = head
        val list = ArrayList<Interceptor>()
        while (cur != null) {
            list.add(cur)
            cur = cur.nextInterceptor
        }
        return list
    }

    private fun findTail() {
        var cur = head
        while (cur?.nextInterceptor != null) cur = cur.nextInterceptor
        tail = cur
    }
}