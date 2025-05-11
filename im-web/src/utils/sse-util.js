import { EventSourcePolyfill } from 'event-source-polyfill';

class SSETool {
    constructor(options) {
        // 默认配置
        const defaultOptions = {
            url: '',              // SSE连接地址（必填）
            token: '',            // 鉴权Token（可选）
            retryInterval: 5000,  // 重试间隔（毫秒）
            heartbeatInterval: 30000, // 心跳检测间隔（可选）
            onOpen: () => {},     // 连接成功回调
            onError: (err) => {}, // 错误回调
        };

        this.options = { ...defaultOptions, ...options };
        this.eventSource = null;
        this.listeners = new Map(); // 存储事件监听器 { eventName: [callback1, callback2...] }
        this.reconnectAttempts = 0;
        this.heartbeatTimer = null;

        // 初始化连接
        this.connect();
    }

    // 建立SSE连接
    connect() {
        // 关闭旧连接
        if (this.eventSource) {
            this.close();
        }

        // 构造请求头（携带Token）
        const headers = {};
        if (this.options.token) {
            headers['token'] = `${this.options.token}`;
        }

        // 使用EventSourcePolyfill支持自定义Header
        this.eventSource = new EventSourcePolyfill(this.options.url, {
            headers: headers,
            heartbeatTimeout: this.options.heartbeatInterval,
        });

        // 连接成功
        this.eventSource.onopen = () => {
            this.reconnectAttempts = 0;
            this.options.onOpen();
            //this.startHeartbeat();
        };

        // 统一错误处理
        this.eventSource.onerror = (err) => {
            this.options.onError(err);
            this.handleReconnect();
        };

        // 绑定已注册的事件监听器
        this.listeners.forEach((callbacks, eventName) => {
            callbacks.forEach(cb => {
                this.eventSource.addEventListener(eventName, cb);
            });
        });
    }

    // 自动重连
    handleReconnect() {
        this.reconnectAttempts++;
        setTimeout(() => {
            console.log(`SSE重连尝试第${this.reconnectAttempts}次...`);
            this.connect();
        }, this.options.retryInterval);
    }

    // 心跳检测（可选）
    startHeartbeat() {
        if (this.options.heartbeatInterval) {
            this.heartbeatTimer = setInterval(() => {
                if (this.eventSource.readyState === EventSource.OPEN) {
                    // 发送空注释维持连接
                    this.eventSource.dispatchEvent(new CustomEvent('ping', {
                        bubbles: true,    // 事件是否冒泡
                        cancelable: true, // 事件能否被取消
                        detail: {         // 自定义数据（通过 event.detail 访问）
                            message: ''
                        }
                    }));
                }
            }, this.options.heartbeatInterval);
        }
    }

    // 监听事件
    on(eventName, callback) {
        if (!this.listeners.has(eventName)) {
            this.listeners.set(eventName, []);
        }
        this.listeners.get(eventName).push(callback);

        // 动态添加监听器
        if (this.eventSource) {
            this.eventSource.addEventListener(eventName, callback);
        }
    }

    // 移除事件监听
    off(eventName, callback) {
        if (this.listeners.has(eventName)) {
            const callbacks = this.listeners.get(eventName).filter(cb => cb !== callback);
            this.listeners.set(eventName, callbacks);

            if (this.eventSource) {
                this.eventSource.removeEventListener(eventName, callback);
            }
        }
    }

    // 关闭连接
    close() {
        if (this.eventSource) {
            this.eventSource.close();
            this.eventSource = null;
        }
        if (this.heartbeatTimer) {
            clearInterval(this.heartbeatTimer);
        }
        this.listeners.clear();
    }
}

export default SSETool;