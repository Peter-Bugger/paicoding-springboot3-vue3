
const fs = require('fs');
const outputPath = 'E:/JavaCode/paicoding-springboot3-vue3/.claude/workflows/DESIGN.md';
const parts = [];

// Part headers
parts.push('# 详细设计: 私信功能 (Private Messaging)');
parts.push('');
parts.push('---');
parts.push('');

// ... continue building parts
parts.push('TEST FILE - DESIGN.md generation script');

fs.writeFileSync(outputPath, parts.join('
'), 'utf8');
console.log('DESIGN.md generated successfully');
