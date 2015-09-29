#stops {
  marker-comp-op: screen;
  marker-allow-overlap: false;
  marker-line-width: 0;
  marker-fill: #e90505;
  [zoom>=0] { marker-width: [mag] * [mag] * 0.1; }
  [zoom>=2] { marker-width: [mag] * [mag] * 0.2; }
  [zoom>=3] { marker-width: [mag] * [mag] * 0.4; }
  [zoom>=4] { marker-width: [mag] * [mag] * 0.6; }
  [zoom>=5] { marker-width: [mag] * [mag] * 1; }
  [zoom>=6] { marker-width: [mag] * [mag] * 2; }
  [zoom>=7] { marker-width: [mag] * [mag] * 4; }
  [zoom>=8] { marker-width: [mag] * [mag] * 8; }
  [zoom>=9] { marker-width: [mag] * [mag] * 12; }
  [zoom>=10] { marker-width: [mag] * [mag] * 24; }
  [zoom>=11] { marker-width: [mag] * [mag] * 48; }
}