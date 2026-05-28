// 检查后端服务是否运行
import http from 'http';

const checkBackend = () => {
  const options = {
    hostname: 'localhost',
    port: 8080,
    path: '/actuator/health',
    method: 'GET',
    timeout: 5000
  };

  console.log('正在检查后端服务...');

  const req = http.request(options, (res) => {
    console.log(`✅ 后端服务正在运行 (状态码: ${res.statusCode})`);
    console.log('🚀 你可以访问以下页面进行测试:');
    console.log('   - 登录测试: http://localhost:5174/debug/login');
    console.log('   - 后端测试: http://localhost:5174/debug/backend');
    console.log('   - 快速访问: http://localhost:5174/debug/access');
  });

  req.on('error', (err) => {
    console.log('❌ 后端服务未运行或无法连接');
    console.log('💡 请先启动后端服务:');
    console.log('   Windows: start-backend.bat');
    console.log('   Linux/Mac: ./start-backend.sh');
    console.log('   手动: cd e-commerce-back && mvn spring-boot:run');
  });

  req.on('timeout', () => {
    console.log('⏰ 连接超时，后端服务可能未启动');
    req.destroy();
  });

  req.end();
};

checkBackend();
