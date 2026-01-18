<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Document</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
            font-family: "Inter", "Segoe UI", Arial, sans-serif;
        }

        body {
            background: linear-gradient(135deg, #fbc2eb 0%, #a6c1ee 100%);
            min-height: 100vh;
            display: flex;
            justify-content: center;
            align-items: center;
        }


        .card {
            width: 380px;
            background: rgba(255, 255, 255, 0.8);
            border-radius: 20px;
            overflow: hidden;
            box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
            backdrop-filter: blur(6px);
            transition: transform 0.3s ease, box-shadow 0.3s ease;
        }


        .card-header {
            display: flex;
            align-items: center;
            padding: 16px;
            background: linear-gradient(90deg, #fbc2eb, #a6c1ee);
            color: #fff;
        }

        .avatar {
            width: 55px;
            height: 55px;
            border-radius: 15px;
            background-size: cover;
            background-position: center;
            border: 2px solid #fff;
            margin-right: 14px;
        }

        .up-info {
            display: flex;
            flex-direction: column;
            justify-content: center;
        }

        .up-name {
            font-size: 18px;
            font-weight: 600;
            line-height: 1.2;
        }

        .bot-label {
            font-size: 13px;
            opacity: 0.85;
        }


        .video-preview {
            width: 100%;
            height: 200px;
            background-size: cover;
            background-position: center;
        }

        #box {
            transform: scale(1.3);
            padding: 30px;
        }

        .card-content {
            padding: 16px;
        }

        .video-title {
            font-size: 20px;
            font-weight: 700;
            color: #333;
            margin-bottom: 8px;
        }

        .video-desc {
            font-size: 14px;
            color: #555;
            line-height: 1.4;
            word-break: break-all;
        }
    </style>
</head>

<body>
<div id="box">
    <div class="card">
        <div class="card-header">
            <div class="avatar" style="background-image: url('${avatar}')"></div>
            <div class="up-info">
                <h2 class="up-name">${upName}</h2>
                <p class="bot-label">LoMu-Bot</p>
            </div>
        </div>

        <div class="video-preview" style="background-image: url('${videoBg}')"></div>

        <div class="card-content">
            <h3 class="video-title">${title}</h3>
            <p class="video-desc">${desc}</p>
        </div>
    </div>
</div>
</body>

</html>