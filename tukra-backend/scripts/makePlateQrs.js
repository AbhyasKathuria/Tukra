import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import QRCode from 'qrcode';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

async function makePlateQrs() {
  const outputDir = path.join(__dirname, '..', '..', 'plates');
  if (!fs.existsSync(outputDir)) {
    fs.mkdirSync(outputDir, { recursive: true });
  }

  console.log(`🖨️  [PLATE QR GENERATOR] Generating QR codes in: ${outputDir}`);

  const qrOptions = {
    errorCorrectionLevel: 'H',
    type: 'image/png',
    margin: 2,
    width: 400,
    color: {
      dark: '#2B1A27', // Ink
      light: '#FFFFFF' // White
    }
  };

  const generatedCodes = [];

  for (let i = 1; i <= 10; i++) {
    const code = `PLT-${String(i).padStart(4, '0')}`;
    const filePath = path.join(outputDir, `${code}.png`);

    await QRCode.toFile(filePath, code, qrOptions);
    generatedCodes.push({ code, file: `${code}.png` });
  }

  console.log('✅ Generated 10 Plate QR codes for counter trays:');
  console.table(generatedCodes);
  console.log('💡 Print these QR codes and affix them to physical mess trays or plates.');
}

makePlateQrs().catch(console.error);
