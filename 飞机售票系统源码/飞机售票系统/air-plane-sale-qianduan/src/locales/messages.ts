export type Locale = 'zh-CN' | 'en-US' | 'ja-JP'

type TrendType = 'up' | 'steady' | 'down'
type ReminderType = 'urgent' | 'warning' | 'info'

export interface LocaleMessages {
  themeSettings: {
    sections: {
      theme: string
      fontSize: string
      language: string
      others: string
    }
    toggles: {
      autoRefresh: string
      showAnimations: string
      compactMode: string
      refreshUnit: string
    }
    modes: {
      light: string
      dark: string
      auto: string
    }
    fontSizes: {
      small: string
      medium: string
      large: string
    }
    languages: Record<Locale, string>
  }
  layout: {
    brand: string
    nav: {
      home: string
      orders: string
      userCenter: string
    }
    logout: {
      button: string
      title: string
      message: string
      confirm: string
      cancel: string
    }
    footer: {
      description: string
      copyright: string
    }
  }
  orderManagement: {
    breadcrumb: {
      home: string
      current: string
    }
    hero: {
      badge: string
      title: string
      description: string
      actions: {
        export: string
        batch: string
      }
    }
    metrics: Array<{
      label: string
      value: string
      trend: string
      trendType: TrendType
      icon: string
    }>
    searchPanel: {
      label: string
      title: string
      reset: string
      fields: {
        orderNumber: { label: string; placeholder: string }
        customer: { label: string; placeholder: string }
        status: {
          label: string
          all: string
          completed: string
          pending_payment: string
          pending_travel: string
          cancelled: string
          refunded: string
        }
        date: { label: string; separator: string }
      }
      actions: {
        search: string
        clear: string
      }
    }
    statusPanel: {
      label: string
      title: string
      latest: string
      summary: {
        processing: { label: string; desc: string }
        completed: { label: string; desc: string }
        refunded: { label: string; desc: string }
      }
    }
    reminders: {
      label: string
      action: string
      list: Array<{
        id: string
        title: string
        desc: string
        time: string
        type: ReminderType
        badgeText: string
      }>
    }
    table: {
      sectionLabel: string
      title: string
      export: string
      batch: string
      headers: {
        orderId: string
        customer: string
        route: string
        departure: string
        amount: string
        status: string
        actions: string
      }
      actions: {
        detail: string
        chat: string
      }
    }
    pagination: {
      infoPrefix: string
      infoMiddle: string
      infoSuffix: string
      jump: string
      page: string
    }
    manageView: {
      label: string
      title: string
      back: string
      empty: string
      select: string
      amount: string
      status: string
      actions: {
        modify: string
        cancel: string
      }
    }
    refundView: {
      label: string
      title: string
      back: string
      empty: string
      select: string
      apply: string
      applied: string
    }
    detailModal: {
      title: string
      fields: {
        customer: string
        route: string
        departure: string
        status: string
        amount: string
      }
    }
    statusMap: {
      completed: string
      pending_payment: string
      pending_travel: string
      cancelled: string
      refunded: string
    }
    alerts: {
      reminder: string
      csvExported: string
      selectOrders: string
      confirmBatch: string
      batchSuccess: string
      chat: string
      modifyPending: string
      modifyFailed: string
      cancelConfirm: string
      cancelSuccess: string
      cancelFailed: string
      refundPrompt: string
      refundSubmitted: string
      refundFailed: string
    }
    csv: {
      headers: string[]
      fileName: string
    }
  }
}

const zhCN: LocaleMessages = {
  themeSettings: {
    sections: {
      theme: '主题模式',
      fontSize: '字体大小',
      language: '语言偏好',
      others: '其他设置'
    },
    toggles: {
      autoRefresh: '自动刷新数据',
      showAnimations: '显示动画效果',
      compactMode: '紧凑模式',
      refreshUnit: '秒'
    },
    modes: {
      light: '浅色',
      dark: '深色',
      auto: '自动'
    },
    fontSizes: {
      small: '小',
      medium: '中',
      large: '大'
    },
    languages: {
      'zh-CN': '简体中文',
      'en-US': 'English',
      'ja-JP': '日本語'
    }
  },
  layout: {
    brand: '飞机售票系统',
    nav: {
      home: '首页',
      orders: '订单管理',
      userCenter: '个人中心'
    },
    logout: {
      button: '退出登录',
      title: '确认退出',
      message: '确定要退出登录吗？',
      confirm: '确定退出',
      cancel: '取消'
    },
    footer: {
      description: '专业的航空票务管理平台',
      copyright: '保留所有权利'
    }
  },
  orderManagement: {
    breadcrumb: {
      home: '首页',
      current: '订单业务管理'
    },
    hero: {
      badge: '客户体验 · 全渠道',
      title: '订单中台 · 实时监控',
      description: '统一对企业客户、旅客与渠道订单进行检索、处理和智能提醒，保障票务履约。',
      actions: {
        export: '导出报表',
        batch: '批量标记'
      }
    },
    metrics: [
      { label: '企业客户', value: '126', trend: '+12.5%', trendType: 'up', icon: '🏢' },
      { label: '高价值用户', value: '412', trend: '+8.3%', trendType: 'up', icon: '⭐' },
      { label: '激活客户', value: '863', trend: '+4.1%', trendType: 'steady', icon: '🔥' }
    ],
    searchPanel: {
      label: '多条件组合检索',
      title: '订单查询',
      reset: '重置条件',
      fields: {
        orderNumber: { label: '订单号', placeholder: '例如：982347' },
        customer: { label: '姓名', placeholder: '李四' },
        status: {
          label: '状态',
          all: '全部状态',
          completed: '已完成',
          pending_payment: '待支付',
          pending_travel: '待出行',
          cancelled: '已取消',
          refunded: '已退款'
        },
        date: { label: '日期范围', separator: '至' }
      },
      actions: {
        search: '开始查询',
        clear: '清空'
      }
    },
    statusPanel: {
      label: '实时洞察',
      title: '状态分布',
      latest: '最新 {count} 条',
      summary: {
        processing: { label: '处理中', desc: '待客服跟进' },
        completed: { label: '已完成', desc: '履约完毕' },
        refunded: { label: '已退款', desc: '售后关闭' }
      }
    },
    reminders: {
      label: '售后提醒',
      action: '立即处理',
      list: [
        {
          id: 'r1',
          title: '临近出行 · 升舱建议',
          desc: '客户：张晨，明日出发，建议联系推荐升舱',
          time: '2小时前',
          type: 'urgent',
          badgeText: '紧急'
        },
        {
          id: 'r2',
          title: '未完成付款',
          desc: '订单 #982347 待确认支付，需在 2 小时内跟进',
          time: '5小时前',
          type: 'warning',
          badgeText: '待处理'
        }
      ]
    },
    table: {
      sectionLabel: '列表 · 支持批量操作',
      title: '订单列表',
      export: '导出 CSV',
      batch: '批量标记',
      headers: {
        orderId: '订单号',
        customer: '姓名',
        route: '航线',
        flightNo: '航班号',
        ticketNo: '机票号',
        departure: '起飞时间',
        amount: '金额',
        status: '状态',
        actions: '操作'
      },
      actions: {
        detail: '删除',
        chat: '删除'
      }
    },
    pagination: {
      infoPrefix: '共',
      infoMiddle: '条，每页',
      infoSuffix: '条',
      jump: '前往',
      page: '页'
    },
    manageView: {
      label: '机票管理',
      title: '改签或取消机票',
      back: '返回订单列表',
      empty: '暂无可管理的订单',
      select: '选择要管理的订单：',
      amount: '金额：',
      status: '状态：',
      actions: {
        modify: '改签',
        cancel: '取消'
      }
    },
    refundView: {
      label: '退订申请',
      title: '申请退票和退款',
      back: '返回订单列表',
      empty: '暂无可退订的订单',
      select: '选择要退订的订单：',
      apply: '申请退订',
      applied: '已退订'
    },
    detailModal: {
      title: '订单详情',
      fields: {
        customer: '客户：',
        route: '航线：',
        departure: '起飞时间：',
        status: '状态：',
        amount: '金额：'
      }
    },
    statusMap: {
      completed: '已完成',
      pending_payment: '待支付',
      pending_travel: '待出行',
      cancelled: '已取消',
      refunded: '已退款'
    },
    alerts: {
      reminder: '处理提醒：{title}',
      csvExported: 'CSV文件已导出',
      selectOrders: '请选择要标记的订单',
      confirmBatch: '确定要标记选中的 {count} 条订单吗？',
      batchSuccess: '已标记 {count} 条订单',
      chat: '打开与订单 #{id} 的沟通记录',
      modifyPending: '改签订单 #{id} 的功能开发中',
      modifyFailed: '改签失败，请重试',
      cancelConfirm: '确定要取消订单 #{id} 吗？',
      cancelSuccess: '订单 #{id} 已取消',
      cancelFailed: '取消失败，请重试',
      refundPrompt: '请输入退订原因：',
      refundSubmitted: '订单 #{id} 的退订申请已提交',
      refundFailed: '退订申请失败，请重试'
    },
    csv: {
      headers: ['订单号', '姓名', '航线', '航班号', '机票号', '起飞时间', '状态', '金额'],
      fileName: '订单列表'
    }
  }
} as const

const enUS: LocaleMessages = {
  themeSettings: {
    sections: {
      theme: 'Theme Mode',
      fontSize: 'Font Size',
      language: 'Language Preference',
      others: 'Other Settings'
    },
    toggles: {
      autoRefresh: 'Auto refresh data',
      showAnimations: 'Show animations',
      compactMode: 'Compact mode',
      refreshUnit: 'seconds'
    },
    modes: {
      light: 'Light',
      dark: 'Dark',
      auto: 'Auto'
    },
    fontSizes: {
      small: 'Small',
      medium: 'Medium',
      large: 'Large'
    },
    languages: {
      'zh-CN': 'Simplified Chinese',
      'en-US': 'English',
      'ja-JP': 'Japanese'
    }
  },
  layout: {
    brand: 'Air Ticketing System',
    nav: {
      home: 'Home',
      orders: 'Orders',
      userCenter: 'Profile'
    },
    logout: {
      button: 'Sign out',
      title: 'Confirm Sign-out',
      message: 'Are you sure you want to sign out?',
      confirm: 'Sign out',
      cancel: 'Cancel'
    },
    footer: {
      description: 'Professional air ticket management platform',
      copyright: 'All rights reserved'
    }
  },
  orderManagement: {
    breadcrumb: {
      home: 'Home',
      current: 'Order Operations'
    },
    hero: {
      badge: 'Customer Experience · Omnichannel',
      title: 'Order Control Center · Live Monitoring',
      description: 'Search, process, and monitor enterprise and traveler orders across channels to ensure fulfillment.',
      actions: {
        export: 'Export report',
        batch: 'Batch tagging'
      }
    },
    metrics: [
      { label: 'Enterprise clients', value: '126', trend: '+12.5%', trendType: 'up', icon: '🏢' },
      { label: 'High-value users', value: '412', trend: '+8.3%', trendType: 'up', icon: '⭐' },
      { label: 'Activated customers', value: '863', trend: '+4.1%', trendType: 'steady', icon: '🔥' }
    ],
    searchPanel: {
      label: 'Multi-condition search',
      title: 'Order Search',
      reset: 'Reset filters',
      fields: {
        orderNumber: { label: 'Order No.', placeholder: 'e.g. 982347' },
        customer: { label: 'Customer name', placeholder: 'Company or traveler' },
        status: {
          label: 'Status',
          all: 'All statuses',
          processing: 'Processing',
          completed: 'Completed',
          refunded: 'Refunded'
        },
        date: { label: 'Date range', separator: 'to' }
      },
      actions: {
        search: 'Search',
        clear: 'Clear'
      }
    },
    statusPanel: {
      label: 'Live insights',
      title: 'Status distribution',
      latest: 'Latest {count}',
      summary: {
        processing: { label: 'Processing', desc: 'Awaiting agent follow-up' },
        completed: { label: 'Completed', desc: 'Fulfilled' },
        refunded: { label: 'Refunded', desc: 'After-sales closed' }
      }
    },
    reminders: {
      label: 'After-sales reminders',
      action: 'Handle now',
      list: [
        {
          id: 'r1',
          title: 'Upcoming flight · Upgrade advice',
          desc: 'Customer Zhang Chen departs tomorrow, recommend an upgrade call',
          time: '2 hours ago',
          type: 'urgent',
          badgeText: 'Urgent'
        },
        {
          id: 'r2',
          title: 'Payment pending',
          desc: 'Order #982347 awaits payment confirmation, follow up within 2 hours',
          time: '5 hours ago',
          type: 'warning',
          badgeText: 'Pending'
        }
      ]
    },
    table: {
      sectionLabel: 'List · Bulk operations ready',
      title: 'Order list',
      export: 'Export CSV',
      batch: 'Batch tagging',
      headers: {
        orderId: 'Order No.',
        customer: 'Customer',
        route: 'Route',
        flightNo: 'Flight No.',
        ticketNo: 'Ticket No.',
        departure: 'Departure time',
        amount: 'Amount',
        status: 'Status',
        actions: 'Actions'
      },
      actions: {
        detail: 'Details',
        chat: 'Chat'
      }
    },
    pagination: {
      infoPrefix: 'Total',
      infoMiddle: 'items ·',
      infoSuffix: 'per page',
      jump: 'Go to',
      page: 'page'
    },
    manageView: {
      label: 'Ticket management',
      title: 'Change or cancel tickets',
      back: 'Back to orders',
      empty: 'No orders to manage',
      select: 'Select an order to manage:',
      amount: 'Amount:',
      status: 'Status:',
      actions: {
        modify: 'Change flight',
        cancel: 'Cancel'
      }
    },
    refundView: {
      label: 'Refund requests',
      title: 'Request refunds',
      back: 'Back to orders',
      empty: 'No orders available for refund',
      select: 'Select an order to refund:',
      apply: 'Request refund',
      applied: 'Refunded'
    },
    detailModal: {
      title: 'Order details',
      fields: {
        customer: 'Customer:',
        route: 'Route:',
        departure: 'Departure:',
        status: 'Status:',
        amount: 'Amount:'
      }
    },
    statusMap: {
      processing: 'Processing',
      completed: 'Completed',
      refunded: 'Refunded'
    },
    alerts: {
      reminder: 'Handle reminder: {title}',
      csvExported: 'CSV exported',
      selectOrders: 'Select at least one order',
      confirmBatch: 'Tag the selected {count} orders?',
      batchSuccess: '{count} orders tagged',
      chat: 'Open chat for order #{id}',
      modifyPending: 'Change flow for order #{id} is under development',
      modifyFailed: 'Change failed, try again',
      cancelConfirm: 'Cancel order #{id}?',
      cancelSuccess: 'Order #{id} cancelled',
      cancelFailed: 'Cancel failed, try again',
      refundPrompt: 'Enter a cancellation reason:',
      refundSubmitted: 'Refund submitted for order #{id}',
      refundFailed: 'Refund request failed, try again'
    },
    csv: {
      headers: ['Order No.', 'Customer', 'Route', 'Flight No.', 'Ticket No.', 'Departure', 'Status', 'Amount'],
      fileName: 'order-list'
    }
  }
}

const jaJP: LocaleMessages = {
  themeSettings: {
    sections: {
      theme: 'テーマモード',
      fontSize: '文字サイズ',
      language: '言語設定',
      others: 'その他の設定'
    },
    toggles: {
      autoRefresh: 'データを自動更新',
      showAnimations: 'アニメーションを表示',
      compactMode: 'コンパクト表示',
      refreshUnit: '秒'
    },
    modes: {
      light: 'ライト',
      dark: 'ダーク',
      auto: '自動'
    },
    fontSizes: {
      small: '小',
      medium: '中',
      large: '大'
    },
    languages: {
      'zh-CN': '簡体字中国語',
      'en-US': '英語',
      'ja-JP': '日本語'
    }
  },
  layout: {
    brand: '航空予約システム',
    nav: {
      home: 'ホーム',
      orders: '注文管理',
      userCenter: 'マイページ'
    },
    logout: {
      button: 'ログアウト',
      title: 'ログアウト確認',
      message: 'ログアウトしてもよろしいですか？',
      confirm: 'ログアウト',
      cancel: 'キャンセル'
    },
    footer: {
      description: 'プロフェッショナルな航空券管理プラットフォーム',
      copyright: '全著作権所有'
    }
  },
  orderManagement: {
    breadcrumb: {
      home: 'ホーム',
      current: '注文オペレーション'
    },
    hero: {
      badge: '顧客体験 · オムニチャネル',
      title: '注文プラットフォーム · リアル監視',
      description: '企業・旅行者・チャネルの注文を統合的に検索・処理・通知し、履行を保証します。',
      actions: {
        export: 'レポート出力',
        batch: '一括タグ付け'
      }
    },
    metrics: [
      { label: '法人顧客', value: '126', trend: '+12.5%', trendType: 'up', icon: '🏢' },
      { label: '価値顧客', value: '412', trend: '+8.3%', trendType: 'up', icon: '⭐' },
      { label: 'アクティブ顧客', value: '863', trend: '+4.1%', trendType: 'steady', icon: '🔥' }
    ],
    searchPanel: {
      label: '複数条件検索',
      title: '注文検索',
      reset: '条件をリセット',
      fields: {
        orderNumber: { label: '注文番号', placeholder: '例: 982347' },
        customer: { label: '顧客名', placeholder: '企業または旅客' },
        status: {
          label: '状態',
          all: 'すべて',
          processing: '処理中',
          completed: '完了',
          refunded: '返金済み'
        },
        date: { label: '期間', separator: '〜' }
      },
      actions: {
        search: '検索',
        clear: 'クリア'
      }
    },
    statusPanel: {
      label: 'リアルタイム洞察',
      title: '状態分布',
      latest: '最新 {count} 件',
      summary: {
        processing: { label: '処理中', desc: 'カスタマー連絡待ち' },
        completed: { label: '完了', desc: '履行済み' },
        refunded: { label: '返金済み', desc: 'アフタークローズ' }
      }
    },
    reminders: {
      label: 'アフターリマインダー',
      action: '今すぐ対応',
      list: [
        {
          id: 'r1',
          title: '出発間近 · アップグレード提案',
          desc: '顧客: 張晨、明日出発。アップグレード提案を連絡',
          time: '2時間前',
          type: 'urgent',
          badgeText: '至急'
        },
        {
          id: 'r2',
          title: '未払い注文',
          desc: '注文 #982347 が支払待ち。2時間以内にフォロー',
          time: '5時間前',
          type: 'warning',
          badgeText: '要対応'
        }
      ]
    },
    table: {
      sectionLabel: 'リスト · 一括操作対応',
      title: '注文リスト',
      export: 'CSV出力',
      batch: '一括タグ',
      headers: {
        orderId: '注文番号',
        customer: '顧客',
        route: '路線',
        departure: '出発時間',
        amount: '金額',
        status: '状態',
        actions: '操作'
      },
      actions: {
        detail: '詳細',
        chat: 'チャット'
      }
    },
    pagination: {
      infoPrefix: '合計',
      infoMiddle: '件 · 1ページあたり',
      infoSuffix: '件',
      jump: '移動',
      page: 'ページ'
    },
    manageView: {
      label: 'チケット管理',
      title: '変更・キャンセル手続き',
      back: 'リストに戻る',
      empty: '操作できる注文はありません',
      select: '操作する注文を選択：',
      amount: '金額：',
      status: '状態：',
      actions: {
        modify: '変更',
        cancel: 'キャンセル'
      }
    },
    refundView: {
      label: '払戻申請',
      title: '払戻と返金を申請',
      back: 'リストに戻る',
      empty: '払戻可能な注文はありません',
      select: '払戻する注文を選択：',
      apply: '払戻申請',
      applied: '払戻済み'
    },
    detailModal: {
      title: '注文詳細',
      fields: {
        customer: '顧客：',
        route: '路線：',
        departure: '出発時間：',
        status: '状態：',
        amount: '金額：'
      }
    },
    statusMap: {
      processing: '処理中',
      completed: '完了',
      refunded: '返金済み'
    },
    alerts: {
      reminder: 'リマインダー処理：{title}',
      csvExported: 'CSVを出力しました',
      selectOrders: '注文を選択してください',
      confirmBatch: '{count} 件の注文にタグ付けしますか？',
      batchSuccess: '{count} 件の注文をタグ付けしました',
      chat: '注文 #{id} のチャットを開く',
      modifyPending: '注文 #{id} の変更機能は開発中',
      modifyFailed: '変更に失敗しました。再試行してください',
      cancelConfirm: '注文 #{id} をキャンセルしますか？',
      cancelSuccess: '注文 #{id} をキャンセルしました',
      cancelFailed: 'キャンセルに失敗しました。再試行してください',
      refundPrompt: '払戻理由を入力してください：',
      refundSubmitted: '注文 #{id} の払戻申請を送信しました',
      refundFailed: '払戻申請に失敗しました。再試行してください'
    },
    csv: {
      headers: ['注文番号', '顧客', '路線', '出発時間', '状態', '金額'],
      fileName: '注文リスト'
    }
  }
}

export const messages: Record<Locale, LocaleMessages> = {
  'zh-CN': zhCN,
  'en-US': enUS,
  'ja-JP': jaJP
}

