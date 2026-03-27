INSERT INTO auto_diagram.workflows (id, code, name, description, content, create_time, modify_time, creator, modifier,
                                    deleted)
VALUES (1, 'processon', 'Processon 动态导出流程', 'ProcessOn 自动转存 + 动态格式导出工作流（拟人化操作）', 'name: "Processon动态导出流程"
description: "ProcessOn 自动转存 + 动态格式导出工作流（拟人化操作）"
version: 3

variables:
  baseUrl: "https://www.processon.com"
  # 以下变量由外部传入 (CLI 参数或 API 请求体)
  # url: 分享链接
  # password: 访问密码 (可选)
  # format: 导出格式 (png, png-hd, jpg, jpg-hd, pdf, svg, pos, visio)
  # quality: 品质 (hd, normal)
  # watermark: 水印类型 (system, none, custom)

steps:
  # ===== 阶段一：进入页面（模拟真人浏览）=====
  - id: initial-wait
    type: waitForRandomTimeout
    min: 800
    max: 2500
    description: "页面加载前随机等待"

  - id: go-to-page
    type: navigate
    url: "{{url}}"
    description: "导航到分享页面"

  - id: wait-page-load
    type: waitForRandomTimeout
    min: 1500
    max: 4000
    description: "等待页面初步加载"

  # 模拟真人浏览行为 - 随机滚动
  - id: scroll-down-1
    type: scroll
    direction: down
    amount: 200
    description: "向下滚动查看内容"

  - id: wait-after-scroll-1
    type: waitForRandomTimeout
    min: 600
    max: 1800
    description: "滚动后停留"

  - id: scroll-up-1
    type: scroll
    direction: up
    amount: 100
    description: "向上回滚一点"

  - id: wait-after-scroll-2
    type: waitForRandomTimeout
    min: 400
    max: 1200
    description: "停留后继续"

  # ===== 阶段二：输入密码（如果提供了）=====
  - id: check-and-input-password
    type: conditional
    condition: "{{password}}"
    steps:
      - id: wait-password-input
        type: waitForRandomTimeout
        min: 500
        max: 1500
        description: "找到密码框后犹豫一下"

      - id: move-to-password
        type: moveMouse
        selector: "#po-chart-link-pw > div > input"
        description: "鼠标移动到密码输入框"

      - id: fill-password
        type: fill
        selector: "#po-chart-link-pw > div > input"
        value: "{{password}}"
        description: "输入访问密码"

      - id: wait-before-confirm
        type: waitForRandomTimeout
        min: 300
        max: 800
        description: "输入后停顿"

      - id: move-to-confirm-btn
        type: moveMouse
        selector: "#po-chart-link-pw > div > div.po-button.primary"
        description: "鼠标移动到确认按钮"

      - id: confirm-password
        type: click
        selector: "#po-chart-link-pw > div > div.po-button.primary"
        description: "确认密码"

      - id: wait-after-password
        type: waitForRandomTimeout
        min: 2000
        max: 5000
        description: "等待页面加载"

  # ===== 阶段三：浏览内容后转存（模拟真人先看图再保存）=====
  - id: scroll-view-content
    type: scroll
    direction: down
    amount: 300
    description: "浏览图表内容"

  - id: wait-while-viewing
    type: waitForRandomTimeout
    min: 2000
    max: 6000
    description: "模拟查看内容的时间"

  - id: scroll-back-up
    type: scroll
    direction: up
    amount: 250
    description: "回到顶部"

  - id: wait-before-dump
    type: waitForRandomTimeout
    min: 800
    max: 2000
    description: "准备转存前停顿"

  - id: wait-for-dump-btn
    type: waitForSelector
    selector: "#po-view-header > div.person_left.view-header-left > div.header_handler > span"
    timeout: 15000
    description: "等待转存按钮出现"

  - id: move-mouse-to-dump-btn
    type: moveMouse
    selector: "#po-view-header > div.person_left.view-header-left > div.header_handler > span"
    description: "鼠标移动到转存按钮"

  - id: hover-dump-btn
    type: hover
    selector: "#po-view-header > div.person_left.view-header-left > div.header_handler > span"
    description: "悬停转存按钮"

  - id: wait-dump-menu-stable
    type: waitForRandomTimeout
    min: 1200
    max: 3500
    description: "等待菜单稳定"

  # - id: move-to-dump-option
  #   type: moveMouse
  #   selector: "#po-view-header > div.person_left.view-header-left > div.header_handler > ul > li.dumpFile > div > span.po-diagraming-icons"
  #   description: "鼠标移动到转存选项"

  - id: wait-before-click-dump
    type: waitForRandomTimeout
    min: 400
    max: 1200
    description: "点击前犹豫"

  - id: click-dump-file
    type: click
    selector: "#po-view-header > div.person_left.view-header-left > div.header_handler > ul > li.dumpFile > div > span.po-diagraming-icons"
    description: "点击转存为我的文件"

  # ===== 阶段四：等待转存接口返回 =====
  - id: wait-for-dump-response
    type: waitForResponse
    urlPattern: "/api/personal/chart/share/dump/file/add/chart"
    timeout: 10000
    saveAs: "dumpResponse"
    jsonPath: "data"
    description: "等待转存接口返回"

  - id: wait-after-dump-success
    type: waitForRandomTimeout
    min: 1000
    max: 2500
    description: "转存成功后停顿"

  - id: log-dump-id
    type: log
    message: "转存成功，文件ID: {{dumpResponse_extracted}}"

  # ===== 阶段五：跳转到转存后的编辑页 =====
  - id: navigate-to-editor
    type: navigate
    url: "{{baseUrl}}/diagraming/{{dumpResponse_extracted}}"
    description: "跳转到编辑器页面"

  - id: wait-editor-initial-load
    type: waitForRandomTimeout
    min: 3000
    max: 8000
    description: "编辑器初始加载"

  # 模拟在编辑器中浏览
  - id: scroll-editor-1
    type: scroll
    direction: down
    amount: 150
    description: "在编辑器中浏览"

  - id: wait-while-editing
    type: waitForRandomTimeout
    min: 1500
    max: 4000
    description: "模拟查看编辑状态"

  - id: scroll-editor-2
    type: scroll
    direction: up
    amount: 100
    description: "调整视图位置"

  - id: wait-editor-load
    type: waitForRandomTimeout
    min: 5000
    max: 12000
    description: "等待编辑器完全加载"

#  # 先检测引导弹窗是否存在
#  - id: detect-guide-modal
#    type: evaluate
#    script: |
#      () => {
#        const guideModal = document.querySelector(\'body > div.po-guide > div.po-guide-step > div.step\');
#        return guideModal !== null && guideModal.style.display !== \'none\';
#      }
#    saveAs: "guideModalExists"
#    description: "检测引导弹窗是否存在"
#
#  - id: wait-after-detect
#    type: waitForRandomTimeout
#    min: 300
#    max: 800
#    description: "检测后短暂等待"
#
#  # 如果存在引导弹窗，则关闭它
#  - id: check-guide-modal
#    type: conditional
#    condition: "{{guideModalExists}}"
#    steps:
#      - id: wait-guide-modal
#        type: waitForRandomTimeout
#        min: 500
#        max: 1200
#        description: "引导弹窗出现后停顿"
#
#      - id: move-to-guide-skip
#        type: moveMouse
#        selector: "body > div.po-guide > div.po-guide-step > div.step"
#        description: "鼠标移动到跳过按钮"
#
#      - id: click-guide-skip
#        type: click
#        selector: "body > div.po-guide > div.po-guide-step > div.step"
#        description: "点击跳过引导弹窗"
#
#      - id: wait-after-close-guide
#        type: waitForRandomTimeout
#        min: 800
#        max: 2000
#        description: "关闭引导后等待"

  # ===== 阶段六：动态格式导出（模拟真人导出操作）=====
  - id: wait-export-icon
    type: waitForSelector
    selector: "#header-export > span"
    timeout: 30000
    description: "等待导出图标出现"

  - id: wait-before-export
    type: waitForRandomTimeout
    min: 1000
    max: 3000
    description: "准备导出前停顿"

  - id: move-to-export-icon
    type: moveMouse
    selector: "#header-export > span"
    description: "鼠标移动到导出图标"

  - id: wait-on-export-icon
    type: waitForRandomTimeout
    min: 500
    max: 1500
    description: "悬停前停顿"

  - id: click-export-icon
    type: click
    selector: "#header-export > span"
    description: "点击导出图标"

  - id: wait-export-menu
    type: waitForSelector
    selector: "#header-export-menu"
    timeout: 10000
    description: "等待导出菜单出现"

  - id: wait-before-select-format
    type: waitForRandomTimeout
    min: 800
    max: 2200
    description: "等待菜单稳定"

  # 使用策略模式执行导出
  - id: export-by-strategy
    type: exportWithStrategy
    format: "{{format}}"
    quality: "{{quality}}"
    watermark: "{{watermark}}"
    description: "根据指定格式执行导出"

  # ===== 阶段七：下载后处理 =====
  - id: wait-after-download
    type: waitForRandomTimeout
    min: 2000
    max: 5000
    description: "下载完成后随机等待"

  - id: rename-downloaded-file
    type: renameDownload
    description: "重命名下载文件为：毫秒时间戳+uuid+文件后缀"

  - id: scroll-final
    type: scroll
    direction: down
    amount: 100
    description: "最后滚动一下"

  - id: wait-final
    type: waitForRandomTimeout
    min: 1000
    max: 2500
    description: "结束前停顿"

  - id: workflow-complete
    type: log
    message: "=== ProcessOn 导出完成 | 格式: {{format}} | 文件: {{downloads.0}} ==="
', now(), now(), 'system', 'system', 0);
