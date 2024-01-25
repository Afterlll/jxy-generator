export default [
  {
    path: '/user',
    layout: false,
    routes: [
      { path: '/user/login', component: './User/Login', name: '登录页' },
      { path: '/user/register', component: './User/Register', name: '注册页' },
    ],
  },
  { path: '/', icon: 'home', component: './Index', name: '主页' },
  {
    path: '/admin',
    icon: 'crown',
    name: '管理页',
    access: 'canAdmin',
    routes: [
      { path: '/admin', redirect: '/admin/user' },
      { icon: 'table', path: '/admin/user', component: './Admin/User', name: '用户管理' },
      {
        icon: 'tools',
        path: '/admin/generator',
        component: './Admin/Generator',
        name: '代码生成器管理',
      },
    ],
  },
  { path: '*', layout: false, component: './404' },
];
