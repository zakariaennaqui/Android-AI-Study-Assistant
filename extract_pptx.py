import zipfile
import xml.etree.ElementTree as ET
import sys
import re

with open('slides_text.txt', 'w', encoding='utf-8') as out:
    try:
        with zipfile.ZipFile(r'c:\Users\u1602\AndroidStudioProjects\ai_study_assistant_android\AI_Study_Assistant_Presentation.pptx') as z:
            slide_files = [f for f in z.namelist() if f.startswith('ppt/slides/slide') and f.endswith('.xml')]
            slide_files.sort(key=lambda x: int(re.search(r'slide(\d+)\.xml', x).group(1)))
            
            for idx, slide_file in enumerate(slide_files):
                out.write(f'\n--- SLIDE {idx + 1} ---\n')
                xml_content = z.read(slide_file)
                tree = ET.fromstring(xml_content)
                for node in tree.iter():
                    if node.tag.endswith('}t') and node.text:
                        out.write(node.text + '\n')
    except Exception as e:
        out.write(f'Error: {e}\n')
