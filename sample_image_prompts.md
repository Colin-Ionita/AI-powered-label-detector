# Sample Image Generation Prompts

Use these prompts to generate realistic sample alcohol label images for the take-home prototype.

Recommended approach: use these as visual starting points, then inspect every generated image at full size. If the model changes required text, especially the government warning, regenerate or overlay the exact text afterward. Compliance OCR demos are judged on readable, exact text more than photorealism.

## Global Prompt Add-On

Append this to every prompt:

```text
Create a flat, front-facing alcohol product label image, not on a bottle, no perspective tilt unless requested. Vertical 2:3 aspect ratio, high resolution, crisp OCR-readable typography, high contrast, no decorative text beyond the exact text requested. Do not invent extra brands, warnings, awards, dates, barcodes, signatures, or legal copy. Keep all required text large enough to read.
```

## 1. Happy Path Bourbon

Filename: `bourbon-label.jpg`

```text
Design a clean, premium bourbon label on warm white paper with black and dark copper typography.

Exact label text:
OLD TOM DISTILLERY
Kentucky Straight Bourbon Whiskey
45% Alc./Vol. (90 Proof)
750 mL
Bottled by Old Tom Distillery, Louisville, KY

GOVERNMENT WARNING: (1) According to the Surgeon General, women should not drink alcoholic beverages during pregnancy because of the risk of birth defects. (2) Consumption of alcoholic beverages impairs your ability to drive a car or operate machinery, and may cause health problems.

The phrase GOVERNMENT WARNING: must be uppercase and bold. The warning body must be readable.
```

## 2. Dave's Casing Difference

Filename: `stone-label.jpg`

```text
Design a simple craft spirits label with a slate blue border and cream background.

Exact label text:
STONE'S THROW
Straight Bourbon Whiskey
40% Alc./Vol. (80 Proof)
750 mL
Bottled by Stone's Throw Distilling, Richmond, VA

GOVERNMENT WARNING: (1) According to the Surgeon General, women should not drink alcoholic beverages during pregnancy because of the risk of birth defects. (2) Consumption of alcoholic beverages impairs your ability to drive a car or operate machinery, and may cause health problems.

The brand must appear exactly as STONE'S THROW in all caps. The warning heading must be uppercase and bold.
```

## 3. Wrong ABV Failure

Filename: `wrong-abv.jpg`

```text
Design a bourbon label matching Old Tom Distillery style, cream paper, black typography, copper accent border.

Exact label text:
OLD TOM DISTILLERY
Kentucky Straight Bourbon Whiskey
42% Alc./Vol. (84 Proof)
750 mL
Bottled by Old Tom Distillery, Louisville, KY

GOVERNMENT WARNING: (1) According to the Surgeon General, women should not drink alcoholic beverages during pregnancy because of the risk of birth defects. (2) Consumption of alcoholic beverages impairs your ability to drive a car or operate machinery, and may cause health problems.

The warning heading must be uppercase and bold. All text must be readable.
```

## 4. Warning Title-Case Failure

Filename: `warning-titlecase.jpg`

```text
Design a bourbon label matching Old Tom Distillery style, cream paper, black typography, copper accent border.

Exact label text:
OLD TOM DISTILLERY
Kentucky Straight Bourbon Whiskey
45% Alc./Vol. (90 Proof)
750 mL
Bottled by Old Tom Distillery, Louisville, KY

Government Warning: (1) According to the Surgeon General, women should not drink alcoholic beverages during pregnancy because of the risk of birth defects. (2) Consumption of alcoholic beverages impairs your ability to drive a car or operate machinery, and may cause health problems.

Important: the warning heading must be exactly Government Warning: in title case, not all caps.
```

## 5. Missing Net Contents

Filename: `missing-net.jpg`

```text
Design a bourbon label matching Old Tom Distillery style, cream paper, black typography, copper accent border.

Exact label text:
OLD TOM DISTILLERY
Kentucky Straight Bourbon Whiskey
45% Alc./Vol. (90 Proof)
Bottled by Old Tom Distillery, Louisville, KY

GOVERNMENT WARNING: (1) According to the Surgeon General, women should not drink alcoholic beverages during pregnancy because of the risk of birth defects. (2) Consumption of alcoholic beverages impairs your ability to drive a car or operate machinery, and may cause health problems.

Do not include any net contents such as 750 mL, 700 mL, 1 L, or fl oz anywhere on the label.
```

## 6. Poor Image Quality

Filename: `blurry-label.jpg`

```text
Create a photographed label image of OLD TOM DISTILLERY bourbon with bad lighting, slight motion blur, glare across the lower text, and a mild angle.

The visible label should contain:
OLD TOM DISTILLERY
Kentucky Straight Bourbon Whiskey
45% Alc./Vol. (90 Proof)
750 mL
Bottled by Old Tom Distillery, Louisville, KY
GOVERNMENT WARNING: followed by a long health warning paragraph

Make the image realistically hard to read but not completely blank. It should look like a bad phone photo submitted by an applicant.
```

## 7. Imported Product

Filename: `import-label.jpg`

```text
Design a clean imported whisky label with white paper, black typography, and a small red maple accent.

Exact label text:
OLD TOM DISTILLERY
Canadian Whisky
45% Alc./Vol. (90 Proof)
750 mL
Product of Canada
Imported by Old Tom Imports, Buffalo, NY

GOVERNMENT WARNING: (1) According to the Surgeon General, women should not drink alcoholic beverages during pregnancy because of the risk of birth defects. (2) Consumption of alcoholic beverages impairs your ability to drive a car or operate machinery, and may cause health problems.

The country of origin line Product of Canada must be clear and readable.
```

## 8. Truncated Warning

Filename: `truncated-warning.jpg`

```text
Design a bourbon label matching Old Tom Distillery style.

Exact label text:
OLD TOM DISTILLERY
Kentucky Straight Bourbon Whiskey
45% Alc./Vol. (90 Proof)
750 mL
Bottled by Old Tom Distillery, Louisville, KY

GOVERNMENT WARNING: (1) According to the Surgeon General, women should not drink alcoholic beverages during pregnancy because of the risk of birth defects.

Do not include section (2) of the warning. The warning must visibly stop after the first sentence.
```

