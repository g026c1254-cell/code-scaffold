import Vue from 'vue'
import VueI18n from 'vue-i18n'

Vue.use(VueI18n)

const supportedLocales = ['zh-CN', 'ja-JP']
const savedLocale = localStorage.getItem('locale')
const locale = supportedLocales.includes(savedLocale) ? savedLocale : 'zh-CN'

const i18n = new VueI18n({
  locale,
  fallbackLocale: 'zh-CN',
  messages: {
    'zh-CN': {
      language: {
        chinese: '中文',
        japanese: '日本語'
      },
      nav: {
        home: '首页',
        goods: '商品列表',
        person: '个人中心',
        admin: '管理后台',
        cart: '购物车',
        login: '登录',
        register: '注册',
        logout: '退出登录',
        language: '语言'
      },
      common: {
        profile: '个人信息',
        username: '用户名',
        name: '姓名',
        phone: '电话',
        email: '邮箱',
        address: '地址',
        gender: '性别',
        male: '男',
        female: '女',
        age: '年龄',
        introduction: '个人介绍',
        balance: '余额',
        save: '保存',
        changePassword: '修改密码',
        recharge: '充值',
        amount: '金额',
        paymentMethod: '支付方式',
        cancel: '取消',
        confirm: '确定',
        publishNotice: '发布公告',
        publishProduct: '我要上架商品',
        title: '标题',
        content: '内容',
        publish: '发布',
        productName: '商品名称',
        productCategory: '商品分类',
        price: '价格',
        stock: '库存',
        productImage: '商品图片',
        description: '商品描述',
        submitProduct: '提交上架',
        publisher: '发布人',
        anonymous: '匿名用户',
        productDetail: '商品详情',
        listedAt: '上架时间',
        detailIntroduction: '详细介绍',
        purchaseNotice: '购买须知',
        purchaseGuide: '购买说明',
        genuine: '正品保证',
        returnPolicy: '7天无理由退货',
        freeShipping: '全国包邮',
        afterSales: '售后无忧',
        buyNow: '立即购买',
        favorite: '收藏',
        favorited: '已收藏',
        notice: '公告信息',
        noticeCategory: '公告',
        maintenance: '维护通知',
        campaign: '活动公告',
        viewMore: '查看更多>>',
        newArrivals: '新品上架',
        noNotice: '暂无公告',
        productInfo: '商品信息',
        personalInfo: '个人信息',
        orders: '我的订单',
        collection: '我的收藏',
        category: '商品分类信息',
        userManagement: '用户管理',
        infoManagement: '信息管理',
        systemManagement: '系统管理',
        adminInfo: '管理员信息',
        userInfo: '用户信息',
        carousel: '轮播图信息',
        orderInfo: '订单信息',
        searchProducts: '搜索商品',
        myActivity: '我的内容',
        myProducts: '我发布的商品',
        myNotices: '我发布的公告',
        editNotice: '编辑公告',
        publishTime: '发布时间',
        noMyNotices: '您还没有发布公告',
        noticeUpdateSuccess: '公告已更新',
        noticeUpdateFailed: '公告更新失败',
        listed: '已上架',
        noMyProducts: '您还没有发布商品',
        cancelCollection: '取消收藏',
        noCollection: '您还没有收藏商品',
        unpaid: '待支付',
        purchased: '已购买',
        quantity: '数量',
        orderTime: '下单时间',
        operation: '操作',
        pay: '支付',
        status: '状态',
        noUnpaid: '没有待支付订单',
        noPurchased: '还没有已购买商品',
        confirmPay: '确认支付该订单吗？',
        paySuccess: '支付成功',
        payFailed: '支付失败'
        ,editProduct: '编辑商品'
        ,republishProduct: '重新发布'
        ,deleteProduct: '删除商品'
        ,republishSuccess: '商品已重新发布'
        ,republishFailed: '商品重新发布失败'
        ,confirmDeleteProduct: '确认删除这个商品吗？'
        ,deleteSuccess: '商品已删除'
        ,deleteFailed: '商品删除失败'
        ,imageOnly: '商品图片必须是图片格式'
        ,imageUploadFailed: '图片上传失败'
        ,loadFailed: '加载失败'
        ,saveSuccess: '保存成功'
        ,saveFailed: '保存失败'
      }
    },
    'ja-JP': {
      language: {
        chinese: '中文',
        japanese: '日本語'
      },
      nav: {
        home: 'ホーム',
        goods: '商品一覧',
        person: 'マイページ',
        admin: '管理画面',
        cart: 'カート',
        login: 'ログイン',
        register: '新規登録',
        logout: 'ログアウト',
        language: '言語'
      },
      common: {
        profile: '個人情報',
        username: 'ユーザー名',
        name: '氏名',
        phone: '電話番号',
        email: 'メールアドレス',
        address: '住所',
        gender: '性別',
        male: '男性',
        female: '女性',
        age: '年齢',
        introduction: '自己紹介',
        balance: '残高',
        save: '保存',
        changePassword: 'パスワード変更',
        recharge: 'チャージ',
        amount: '金額',
        paymentMethod: '支払い方法',
        cancel: 'キャンセル',
        confirm: '確定',
        publishNotice: 'お知らせを投稿',
        publishProduct: '商品を出品',
        title: 'タイトル',
        content: '内容',
        publish: '投稿',
        productName: '商品名',
        productCategory: '商品カテゴリ',
        price: '価格',
        stock: '在庫',
        productImage: '商品画像',
        description: '商品説明',
        submitProduct: '出品する',
        publisher: '投稿者',
        anonymous: '匿名ユーザー',
        productDetail: '商品詳細',
        listedAt: '出品日時',
        detailIntroduction: '商品紹介',
        purchaseNotice: '購入について',
        purchaseGuide: '購入案内',
        genuine: '正規品保証',
        returnPolicy: '7日間返品可能',
        freeShipping: '全国送料無料',
        afterSales: '安心アフターサービス',
        buyNow: '今すぐ購入',
        favorite: 'お気に入り',
        favorited: 'お気に入り済み',
        notice: 'お知らせ',
        noticeCategory: 'お知らせ',
        maintenance: 'メンテナンス',
        campaign: 'キャンペーン',
        viewMore: 'もっと見る>>',
        newArrivals: '新着商品',
        noNotice: 'お知らせはありません',
        productInfo: '商品情報',
        personalInfo: '個人情報',
        orders: '注文履歴',
        collection: 'お気に入り',
        category: '商品カテゴリ',
        userManagement: 'ユーザー管理',
        infoManagement: '情報管理',
        systemManagement: 'システム管理',
        adminInfo: '管理者情報',
        userInfo: 'ユーザー情報',
        carousel: 'カルーセル画像',
        orderInfo: '注文情報',
        searchProducts: '商品を検索',
        myActivity: 'マイアクティビティ',
        myProducts: '出品した商品',
        myNotices: '投稿したお知らせ',
        editNotice: 'お知らせを編集',
        publishTime: '投稿日時',
        noMyNotices: '投稿したお知らせはありません',
        noticeUpdateSuccess: 'お知らせを更新しました',
        noticeUpdateFailed: 'お知らせの更新に失敗しました',
        listed: '出品中',
        noMyProducts: '出品した商品はありません',
        cancelCollection: 'お気に入りを解除',
        noCollection: 'お気に入りの商品はありません',
        unpaid: '未払い',
        purchased: '購入済み',
        quantity: '数量',
        orderTime: '注文日時',
        operation: '操作',
        pay: '支払う',
        status: 'ステータス',
        noUnpaid: '未払いの注文はありません',
        noPurchased: '購入済みの商品はありません',
        confirmPay: 'この注文を支払いますか？',
        paySuccess: '支払いが完了しました',
        payFailed: '支払いに失敗しました'
        ,editProduct: '商品を編集'
        ,republishProduct: '再出品する'
        ,deleteProduct: '商品を削除'
        ,republishSuccess: '商品を再出品しました'
        ,republishFailed: '商品の再出品に失敗しました'
        ,confirmDeleteProduct: 'この商品を削除しますか？'
        ,deleteSuccess: '商品を削除しました'
        ,deleteFailed: '商品の削除に失敗しました'
        ,imageOnly: '商品画像は画像形式で指定してください'
        ,imageUploadFailed: '画像のアップロードに失敗しました'
        ,loadFailed: '読み込みに失敗しました'
        ,saveSuccess: '保存しました'
        ,saveFailed: '保存に失敗しました'
      }
    }
  }
})

export function setLocale(value) {
  if (!supportedLocales.includes(value)) return
  i18n.locale = value
  localStorage.setItem('locale', value)
}

export default i18n
