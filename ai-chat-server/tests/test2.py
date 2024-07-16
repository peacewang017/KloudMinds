import gradio as gr
import requests

def query_flask_server(prompt, file_content):
    url = 'http://127.0.0.1:5000/request'
    payload = {
        'prompt': prompt,
        'file': file_content
    }
    response = requests.post(url, json=payload)
    if response.status_code == 200:
        return response.json()['ai_answer']
    else:
        return f"Error: {response.json().get('error', 'Unknown error')}"

def gradio_interface(prompt, file_content):
    return query_flask_server(prompt, file_content)

iface = gr.Interface(
    fn=gradio_interface,
    inputs=[
        gr.Textbox(lines=2, placeholder="Enter your prompt here..."),
        gr.Textbox(lines=10, placeholder="Paste your file content here...")
    ],
    outputs=gr.Textbox(),
    title="AI Chat Server",
    description="Interact with the AI Chat Server using Gradio"
)

if __name__ == "__main__":
    iface.launch()
