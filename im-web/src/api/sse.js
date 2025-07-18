import { EventSourcePolyfill } from 'event-source-polyfill';

/**
 * 创建SSE连接
 * @param {string} url - 接口地址
 * @param {Object} params - 请求参数
 * @param {Function} onMessage - 消息回调
 * @param {Function} onError - 错误回调
 * @returns {EventSourcePolyfill} - SSE实例
 */
let createSSEConnection = (url, params, onMessage, onError) =>  {
    // 转换参数为URL查询字符串
    const query = new URLSearchParams(params).toString()
    const fullUrl = `${url}?${query}`

    // 创建SSE连接（添加认证头示例）
    const eventSource = new EventSourcePolyfill(fullUrl, {
        headers: {
            'accessToken': sessionStorage.getItem("accessToken")
        },
        heartbeatTimeout: 60 * 1000 // 心跳超时设置
    })

    eventSource.onmessage = (event) => {
        try {
            //const data = JSON.parse(event.data)
            onMessage(event.data)
        } catch (e) {
            console.error('SSE数据解析失败', e)
        }
    }

    eventSource.onerror = (error) => {
        onError(error)
        eventSource.close()
    }

    return eventSource
}

export {
    createSSEConnection
}