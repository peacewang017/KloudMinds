import requests

# AI_char_server
AI_chat_server = 'http://localhost:5000/request'
headers = {
    'Content-Type': 'application/json'
}
data = {
    "content": "海伦·亚当斯·凯勒（英语：Helen Adams Keller，1880年6月27日—1968年6月1日）是出身在美国亚拉巴马州的西塔斯坎比亚的作家、身障人权利倡导者、政治活动家和讲师。她在19个月大的一次疾病中失去了视力和听力。然后，她主要使用家庭手语（英语：Home sign 或 kitchen sign，指听障儿童自发性的手势沟通系统）进行沟通。7岁时，她遇到了教师和终身伙伴安·沙利文（Anne Sullivan），后者教导她学习使用语言，包括阅读和写作。沙利文的第一堂课是在凯勒的手上拼写单词，向她展示她周围物体的名称。她还学会了如何使用 Tadoma 方法沟通并理解他人的谈话。在专科学校和主流学校接受教育后，她就读于哈佛大学拉德克利夫学院，并成为第一个获得文学学士学位的聋盲人。从1924年到1968年，她在美国盲人基金会（American Foundation for the Blind）工作；在此期间，她游览于美国各地，并前往全球35个国家，为视力丧失者提供协助。",
    "prompt": "请用 10 个字简要介绍海伦凯勒的生平"
}

response = requests.post(AI_chat_server, headers=headers, json=data)

if response.status_code == 200:
    print(f"Success: {response.json()}")
else:
    print(f"Error: {response.status_code} - {response.json()}")