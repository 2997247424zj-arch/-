# 飞机售票系统技术架构图

根据现有代码库（Vue 3 + Spring Boot 3 + MySQL）及您提供的模板，生成的技术架构图如下：

```mermaid
graph TD
    %% 样式定义：统一宽度让整体更规整
    classDef unit width:140px,text-align:center;

    subgraph PresentationLayer ["展示层 (Presentation Layer)"]
        direction LR
        HTML["HTML5"] --- CSS["SCSS / CSS"] --- Vue["Vue.js 3.5"] --- Images["Assets / 资源库"] --- Echarts["Echarts数据可视化"]
    end

    subgraph NetworkLayer ["网络层 (Network Layer)"]
        direction LR
        Fetch["Axios 交互"] --- REST["RESTful API"] --- Session["Session 认证"] --- Methods["GET/POST 请求"]
    end

    subgraph ServiceLayer ["服务层 (Service Layer)"]
        direction TB
        subgraph Auth ["公共模块"]
            direction LR
            Login[登录/注册] --- UserProfile[个人中心] --- CommonSms[短信服务]
        end

        subgraph Roles ["业务逻辑中心"]
            direction LR
            subgraph UserSide ["用户端 (Passenger)"]
                direction TB
                UC[航班搜索/导入]
                FC[机票预订/支付]
                OC[订单/退改签]
                SC[座位/特殊服务]
                CP[优惠券/积分]
            end
            subgraph OpSide ["运维端 (Operations)"]
                direction TB
                OBC[行李托运管理]
                OPC[航班运行监控]
                OEC[运行异常管理]
                OMC[运行指标分析]
                OTC[票务变更初审]
            end
            subgraph AdminSide ["管理员端 (Admin)"]
                direction TB
                AUC[用户权限管理]
                AFC[航班调度排班]
                ASC[业务统计报表]
                ATC[机型数据维护]
                AAC[系统公告管理]
                ARC[退改终审复核]
            end
        end
    end

    subgraph StorageLayer ["存储层 (Storage Layer)"]
        direction LR
        MySQL[("MySQL 8.0<br/>(Druid)")] --- Redis[("Redis<br/>(Cache)")]
    end

    subgraph RuntimeEnv ["运行环境 (Runtime Environment)"]
        direction LR
        NodeJS["Node.js"] --- SpringBoot["Spring Boot 3.2"] --- JDK17["JDK 17"]
    end

    %% 层级关联
    PresentationLayer --> NetworkLayer
    NetworkLayer --> ServiceLayer
    ServiceLayer --> StorageLayer
    StorageLayer --> RuntimeEnv

    %% 样式美化
    style PresentationLayer fill:#fff9c4,stroke:#fbc02d,stroke-width:2px
    style NetworkLayer fill:#ffebee,stroke:#ef9a9a,stroke-width:2px
    style ServiceLayer fill:#e3f2fd,stroke:#90caf9,stroke-width:2px
    style StorageLayer fill:#e8f5e9,stroke:#a5d6a7,stroke-width:2px
    style RuntimeEnv fill:#f3e5f5,stroke:#ce93d8,stroke-width:2px
    
    style UserSide fill:#ffffff,stroke:#bbdefb
    style OpSide fill:#ffffff,stroke:#bbdefb
    style AdminSide fill:#ffffff,stroke:#bbdefb
    style Auth fill:#ffffff,stroke:#bbdefb

    %% 应用统一宽度类
    class HTML,CSS,Vue,Images,Echarts,Fetch,REST,Session,Methods,Login,UserProfile,CommonSms,UC,FC,OC,SC,CP,OBC,OPC,OEC,OMC,OTC,AUC,AFC,ASC,ATC,AAC,ARC,MySQL,Redis,NodeJS,SpringBoot,JDK17 unit;
```

## 架构说明

### 1. 展示层 (Presentation Layer)
- **Vue.js 3.5**: 核心前端框架，负责响应式页面渲染。
- **Echarts**: 用于管理员端和运维端的统计报表展示。
- **CSS**: 采用原声 CSS 结合现代布局技术实现精美 UI。

### 2. 网络层 (Network Layer)
- **Fetch/Axios**: 前端通过 `api.ts` 封装的请求库与后端通信。
- **认证机制**: 采用基于 Session 的认证，用户信息存储在本地 `sessionStorage` 中。

### 3. 服务层 (Service Layer)
- **用户端**: 核心功能包括航班搜索、座位预订、订单支付、天气查询等。
- **运维端**: 专注于即时运行数据的监控和行李管理。
- **管理员端**: 负责系统基础数据维护（机型、航班）及业务统计。

### 4. 存储层 (Storage Layer)
- **MySQL**: 存储用户信息、航班排班、订单记录等核心数据。
- **Druid**: 高性能数据库连接池，提供监控和扩展能力。

### 5. 运行环境 (Runtime Environment)
- **Spring Boot 3.2**: 提供 RESTful 接口服务。
- **Node.js**: 用于前端项目的构建与开发调试。
