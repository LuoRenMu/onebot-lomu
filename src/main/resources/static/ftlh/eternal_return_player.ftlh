<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Document</title>
    <link rel="stylesheet" href="/css/eternal_return_record.css">
    <script src="/javascript/chart.js"></script>
</head>
<body>
<div id="content-container">
    <div id="header">
        <div id="banner_user_info">
            <div class="profile-image-wrapper">
                <#if profileImageUrl??>
                    <img src="${profileImageUrl}" alt=""/>
                <#else>
                    <#include "eternal_return_error_img.ftlh">
                </#if>
            </div>
            <div id="top">
                <div class="level">Lv.${level}</div>
                <div class="nickname">${nickName}</div>
                <#if playTime == 0>
                    <p>当前赛季游戏时间： 0小时0分钟</p>
                <#else>
                    <p>当前赛季游戏时间： ${(playTime/60/60)?floor}小时${playTime%60}分钟</p>
                </#if>
            </div>
        </div>
        <div id="describe">
            <div id="logo">Design inspired by DakGG •
                Powered by LuoRenMu
            </div>
        </div>
    </div>
    <div id="body">
        <div id="left">
            <div id="rank">
                <h4>排位(${season})</h4>
                <div id="score">
                    <div id="rp_img">
                        <#if data.tierImageUrl == "">
                            <img src="/images/eternal_return/tier/0" alt="">
                        <#else>
                            <img src="${data.tierImageUrl}" alt="">
                        </#if>
                    </div>
                    <div id="rp_box">
                        <div id="rp">
                            ${data.rp}
                        </div>
                        <div id="rp_name">
                            ${data.rpName}
                        </div>
                    </div>
                </div>
                <div id="record">
                    <div>
                        <div class="record_box">
                            <h4>平均TK</h4>
                            <h4>${data.avgTk}</h4>
                        </div>
                        <div class="record_box">
                            <h4>TOP 1</h4>
                            <h4>${data.top1}</h4>
                        </div>
                        <div class="record_box">
                            <h4>游戏场次</h4>
                            <h4>${data.play}</h4>
                        </div>

                        <div class="record_box">
                            <h4>平均击杀</h4>
                            <h4>${data.avgKill}</h4>
                        </div>
                        <div class="record_box">
                            <h4>TOP 2</h4>
                            <h4>${data.top2}</h4>
                        </div>
                        <div class="record_box">
                            <h4>平均伤害</h4>
                            <h4>${data.avgDmg}</h4>
                        </div>
                        <div class="record_box">
                            <h4>平均助攻</h4>
                            <h4>${data.avgAssists}</h4>
                        </div>
                        <div class="record_box">
                            <h4>TOP 3</h4>
                            <h4>${data.top3}</h4>
                        </div>
                        <div class="record_box">
                            <h4>平均排名</h4>
                            <h4>${data.avgRank}</h4>
                        </div>
                    </div>
                </div>

                <#if mmrStats??>
                    <div id="rank_stats">
                        <canvas id="rank_canvas"></canvas>
                    </div>
                    <script>
                        const ctx = document.getElementById('rank_canvas');
                        const labels = ${mmrStats.mmrDateJson?no_esc};
                        const data = {
                            labels: labels,
                            datasets: [{
                                data: ${mmrStats.mmrJson},
                                fill: false,
                                borderColor: 'rgb(202, 164, 40)',
                                backgroundColor: ['rgb(202, 164, 40)'],
                                pointRadius: 4,
                                tension: 0.1,
                            }]
                        };

                        const config = {
                            type: 'line', // 表类型
                            data: data,
                            options: {
                                plugins: {
                                    legend: {
                                        display: false
                                    }
                                },
                                scales: {
                                    x: {
                                        grid: {
                                            display: false
                                        }
                                    },
                                    y: {
                                        grid: {
                                            display: false
                                        },
                                        ticks: {
                                            stepSize: 100
                                        }
                                    }
                                }
                            }
                        };
                        const myChart = new Chart(ctx, config);
                    </script>
                </#if>


            </div>

            <#if characterUseStats?has_content>
                <section>
                    <table id="rank_character_stats">
                        <thead>
                        <tr>
                            <th class="character">角色</th>
                            <th class="win-rate">胜率</th>
                            <th class="get-rp">RP</th>
                            <th class="avg-rank">平均排名</th>
                            <th class="avg-dmg">平均伤害</th>
                        </tr>
                        </thead>
                        <tbody>
                        <#list characterUseStats as character>
                            <tr>
                                <td class="character">
                                    <div class="image-wrapper"><img
                                                src="${character.imgUrl}"
                                                alt=""></div>
                                    <div class="info">${character.characterName}
                                        <div class="plays">${character.characterPlay} 游戏</div>
                                    </div>
                                </td>
                                <td class="win-rate">${character.winRate}</td>
                                <td class="get-rp">
                                    <#if character.getRP gte 0>
                                        <svg xmlns="http://www.w3.org/2000/svg" width="8" height="5" viewBox="0 0 8 5"
                                             fill="none"
                                             style="transform: none;">
                                            <path d="M6.75 4.75C7.17188 4.75 7.38281 4.25781 7.07812 3.95312L4.07812 0.953125C3.89062 0.765625 3.58594 0.765625 3.39844 0.953125L0.398438 3.95312C0.09375 4.25781 0.304688 4.75 0.726562 4.75H6.75Z"
                                                  fill="#FF4655"></path>
                                        </svg>
                                        ${character.getRP}
                                    <#else>
                                        <svg xmlns="http://www.w3.org/2000/svg" width="8" height="5" viewBox="0 0 8 5"
                                             fill="none" style="transform: rotate(180deg);">
                                            <path
                                                    d="M6.75 4.75C7.17188 4.75 7.38281 4.25781 7.07812 3.95312L4.07812 0.953125C3.89062 0.765625 3.58594 0.765625 3.39844 0.953125L0.398438 3.95312C0.09375 4.25781 0.304688 4.75 0.726562 4.75H6.75Z"
                                                    fill="#5393ca"></path>
                                        </svg>
                                        ${character.getRP}
                                    </#if>
                                </td>
                                <td class="avg-rank">${character.avgRank}</td>
                                <td class="avg-dmg">${character.avgDmg}</td>
                            </tr>
                        </#list>
                        </tbody>
                    </table>
                </section>
            </#if>
            <#if recentPlayers?has_content>
                <section>
                    <table id="recent_play">
                        <thead>
                        <tr>
                            <th class="character">一起游戏的玩家 (最近组排队友)</th>
                            <th class="win-rate">胜率</th>
                            <th class="avg-rank">平均排名</th>
                        </tr>
                        </thead>
                        <tbody>
                        <#list recentPlayers as recentPlayer>
                            <tr>
                                <td class="character">
                                    <div class="image-wrapper"><img
                                                src="${recentPlayer.imageWrapperUrl}"
                                                alt=""></div>
                                    <div class="info">${recentPlayer.nickname}
                                        <div class="plays">${recentPlayer.plays} 游戏</div>
                                    </div>
                                </td>
                                <td class="win-rate">
                                    ${recentPlayer.winRate}
                                </td>
                                <td class="avg-rank">
                                    ${recentPlayer.avgRank}
                                </td>
                            </tr>
                        </#list>

                        </tbody>
                    </table>
                </section>
            </#if>
        </div>
        <div id="right">
            <#if rating??>
                <div id="lomu_rating">
                    ${rating?no_esc}
                </div>
            </#if>
            <#list matches as match>
                <div class="war_record">
                    <div class="game_id">Game ID
                        ${match.serverName}-${match.gameId}
                        (${match.version})
                    </div>
                    <#if match.rank == 99>
                        <div class="war_record_left_escape"></div>
                        <div class="war_record_right_escape"></div>
                    <#elseif match.rank gte 3 || (match.type == "钴协议" && match.rank == 2) >
                        <div class="war_record_left_top3"></div>
                        <div class="war_record_right_top3"></div>
                    <#else>
                        <div class="war_record_left_top${match.rank}"></div>
                        <div class="war_record_right_top${match.rank}"></div>
                    </#if>
                    <div class="war_record1">
                        <div class="war_rank">
                            <#if match.rank == 99>
                                <div style="color:#475482">逃离</div>
                            <#elseif match.type == "钴协议" && match.rank == 1>
                                <div>胜利</div>
                            <#elseif match.type == "钴协议" && match.rank == 2>
                                <div>失败</div>
                            <#elseif match.rank == 1>
                                <div style="color: #11B288">#${match.rank}</div>
                            <#elseif match.rank == 2>
                                <div style="color: #207AC7">#${match.rank}</div>
                            <#else>
                                <div>#${match.rank}</div>
                            </#if>
                            <div>${match.type}</div>
                            <div>${match.dateHour}</div>
                            <div>${match.dateMonth}</div>
                        </div>
                        <div class="war_record_character_info">
                            <div class="hero_avatar">
                                <img src="${match.characterAvatarUrl}"
                                     alt="">
                            </div>
                            <div class="character_name">${match.characterName}</div>
                        </div>
                        <div class="skill">
                            <div class="weapon">
                                <img src="${match.weaponUrl}"
                                     alt="">
                            </div>
                            <div class="trait">
                                <img src="${match.traitSkillUrl}"
                                     alt="">
                            </div>
                            <div class="trait">
                                <img src="${match.skillUrl}"
                                     alt="">
                            </div>

                            <div class="trait">
                                <#if match.type == "钴协议">
                                    <img src="/images/img-placeholder-wilson-round.png"
                                         alt="">
                                <#else>
                                    <img src="${match.traitSkillGroupUrl}"
                                         alt="">
                                </#if>
                            </div>
                        </div>
                        <div class="play_stat">
                            <div class="stat">
                                ${match.tk} <span>/</span> ${match.kill} <span>/</span> ${match.assist}
                            </div>
                            <div class="label">
                                TK <span>/</span> K <span>/</span> A
                            </div>
                        </div>
                        <div class="play_data">
                            <div class="damage">
                                <div class="play_data_title">${match.dmg}</div>
                                <div class="play_data_label">DMG</div>
                            </div>
                            <#if match.type == "排位">
                                <div class="rp">
                                    <div class="play_data_title">${match.rp}
                                        <#if match.rpChange gte 0>
                                            <svg xmlns="http://www.w3.org/2000/svg" width="8" height="5"
                                                 viewBox="0 0 8 5" fill="none"
                                                 style="transform: none;">
                                                <path d="M6.75 4.75C7.17188 4.75 7.38281 4.25781 7.07812 3.95312L4.07812 0.953125C3.89062 0.765625 3.58594 0.765625 3.39844 0.953125L0.398438 3.95312C0.09375 4.25781 0.304688 4.75 0.726562 4.75H6.75Z"
                                                      fill="#FF4655"></path>
                                            </svg>
                                            <span style="color: #FF4655">${match.rpChange}</span>
                                        <#else>
                                            <svg xmlns="http://www.w3.org/2000/svg" width="8" height="5"
                                                 viewBox="0 0 8 5"
                                                 fill="none" style="transform: rotate(180deg);">
                                                <path
                                                        d="M6.75 4.75C7.17188 4.75 7.38281 4.25781 7.07812 3.95312L4.07812 0.953125C3.89062 0.765625 3.58594 0.765625 3.39844 0.953125L0.398438 3.95312C0.09375 4.25781 0.304688 4.75 0.726562 4.75H6.75Z"
                                                        fill="#5393ca"></path>
                                            </svg>
                                            <span style="color: #5393CA">${match.rpChange}</span>
                                        </#if>
                                    </div>
                                    <div class="play_data_label">RP</div>
                                </div>
                            <#else>
                                <div class="rp">
                                    <div class="play_data_title">${match.kda}</div>
                                    <div class="play_data_label">KDA</div>
                                </div>
                            </#if>
                            <div class="route">
                                <div class="play_data_title">${match.routeId}</div>
                                <div class="play_data_label">路径ID
                                </div>
                            </div>
                        </div>

                        <ul class="item_box">
                            <#list match.equips as equip>
                                <li class="item">
                                    <#if equip.itemBgUrl != "" >
                                        <img class="item_bg" src="${equip.itemBgUrl}"
                                             alt="">
                                    </#if>
                                    <#if equip.itemUrl != "" >
                                        <img class="item_img"
                                             src="${equip.itemUrl}" alt="">
                                    </#if>
                                </li>
                            </#list>
                        </ul>
                    </div>
                    <#if match.teamMates??>
                        <#list match.teamMates as teamMate>
                            <div class="war_record2">
                                <div class="teammate_name">
                                    <div class="play_name">${teamMate.nickName}</div>
                                    <div class="teammate_rp">
                                        <div class="teammate_rp_img"><img
                                                    src="${teamMate.rpImageUrl}" alt=""></div>
                                        <span>${teamMate.rp} RP</span>
                                    </div>
                                </div>
                                <div class="hero_avatar">
                                    <img src="${teamMate.avatarUrl}"
                                         alt="">
                                </div>
                                <div class="skill">
                                    <div class="weapon">
                                        <img src="${teamMate.weaponUrl}"
                                             alt="">
                                    </div>
                                    <div class="trait">
                                        <img src="${teamMate.traitSkillUrl}"
                                             alt="">
                                    </div>
                                    <div class="trait">
                                        <img src="${teamMate.skillUrl}"
                                             alt="">
                                    </div>

                                    <div class="trait">
                                        <#if match.type == "钴协议">
                                            <img src="/images/img-placeholder-wilson-round.png"
                                                 alt="">
                                        <#else>
                                            <img src="${teamMate.traitSkillGroupUrl}"
                                                 alt="">
                                        </#if>
                                    </div>
                                </div>
                                <div class="play_stat">
                                    <div class="stat">
                                        ${teamMate.tk} <span>/</span> ${teamMate.kill} <span>/</span> ${teamMate.assist}
                                    </div>
                                    <div class="label">
                                        TK <span>/</span> K <span>/</span> A
                                    </div>
                                </div>
                                <div class="play_data">
                                    <div class="damage">
                                        <div class="play_data_title">${teamMate.dmg}</div>
                                        <div class="play_data_label">DMG</div>
                                    </div>

                                </div>

                                <ul class="item_box">
                                    <#list teamMate.equips as teamMateEquip>
                                        <li class="item">
                                            <img class="item_bg" src="${teamMateEquip.itemBgUrl}"
                                                 alt="">
                                            <img class="item_img"
                                                 src="${teamMateEquip.itemUrl}" alt="">
                                        </li>
                                    </#list>
                                </ul>
                            </div>
                        </#list>
                    </#if>
                </div>

            </#list>
        </div>
    </div>
</div>
</body>

</html>