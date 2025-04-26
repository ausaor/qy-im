<!-- components/tree-node/tree-node.vue -->
<template>
  <view class="tree-node">
    <!-- 当前节点 -->
    <view
        class="node-content"
        :style="contentStyle"
        @click.stop="handleClick"
    >
      <!-- 展开箭头（仅当有子节点时显示） -->
      <view
          class="arrow"
          :class="{ expanded: isExpanded }"
          @click.stop="toggleExpand"
      >
        ▶
      </view>

      <!-- 节点插槽（允许自定义内容） -->
      <slot name="node" :node="node">
        <!-- 默认节点内容 -->
        <text class="node-label">{{ node[labelKey] }}</text>
      </slot>
    </view>

    <!-- 子节点容器（递归渲染） -->
    <view
        v-show="isExpanded"
        v-if="hasChildren"
        class="children-container"
    >
      <tree-node
          v-for="child in node[childrenKey]"
          :key="child[idKey]"
          :node="child"
          :level="level + 1"
          :props="props"
          @node-click="$emit('node-click', $event)"
      >
        <!-- 透传插槽内容 -->
        <template v-slot:node="slotProps">
          <slot name="node" v-bind="slotProps" />
        </template>
      </tree-node>
    </view>
  </view>
</template>

<script>
export default {
  name: 'tree-node', // 必须声明 name 用于递归
  props: {
    node: { type: Object, required: true }, // 当前节点数据
    level: { type: Number, default: 0 },    // 当前层级
    props: {                               // 字段映射配置
      type: Object,
      default: () => ({
        id: 'id',
        label: 'label',
        children: 'children'
      })
    }
  },
  data() {
    return {
      isExpanded: false // 展开状态
    }
  },
  computed: {
    // 动态字段映射
    idKey() { return this.props.id || 'id' },
    labelKey() { return this.props.label || 'label' },
    childrenKey() { return this.props.children || 'children' },

    // 判断是否有子节点
    hasChildren() {
      const children = this.node[this.childrenKey]
      return children && children.length > 0
    },

    // 节点内容样式
    contentStyle() {
      return {
        paddingLeft: `${this.level * 20}px`, // 层级缩进
        backgroundColor: this.level % 2 === 0 ? '#f8f8f8' : '#fff' // 交替背景色
      }
    }
  },
  methods: {
    toggleExpand() {
      if (this.hasChildren) {
        this.isExpanded = !this.isExpanded
        this.$emit('node-toggle', this.node)
      }
    },
    handleClick() {
      this.isExpanded = !this.isExpanded
      this.$emit('node-click', this.node)
    }
  }
}
</script>

<style lang="scss" scoped>
.tree-node {
  font-size: 14px;
}

.node-content {
  padding: 12px 10px;
  border-bottom: 1px solid #eee;
  display: flex;
  align-items: center;
}

.arrow {
  margin-right: 8px;
  transition: transform 0.2s;
  font-size: 12px;
}

.arrow.expanded {
  transform: rotate(90deg);
}

.node-label {
  color: #333;
}
</style>