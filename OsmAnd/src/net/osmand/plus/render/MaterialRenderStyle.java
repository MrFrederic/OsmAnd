package net.osmand.plus.render;

import android.app.UiModeManager;
import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.google.android.material.color.DynamicColors;
import com.materialkolor.dynamiccolor.ColorSpec;
import com.materialkolor.hct.Hct;
import com.materialkolor.palettes.TonalPalette;
import com.materialkolor.scheme.DynamicScheme;
import com.materialkolor.scheme.Variant;

import java.util.Map;

import io.github.mrfrederic.materialmap.MaterialMapPalette;
import io.github.mrfrederic.materialmap.Token;

/**
 * "Material Design" map style: a render.xml depending on default.render.xml whose colour attributes
 * are generated from the device's Material You palettes via material-map-palette tokens.
 */
public class MaterialRenderStyle {

	// token -> default.render.xml colour attributes it overrides
	private static final String[][] ATTRIBUTES = {
			{"urban", "landuseResidentialColor"},
			{"building", "buildingColor", "buildingResidentialColor", "buildingCommercialColor", "buildingOfficeColor",
					"buildingIndustrialColor", "buildingEducationalColor", "buildingHospitalColor"},
			{"buildingLine", "buildingStrokeColor"},
			{"industrial", "landuseIndustrialColor", "landuseCommercialColor", "landuseRetailColor", "landuseRailwayColor",
					"landuseGaragesColor", "landuseConstructionColor", "powerLanduseColor", "portColor", "brownfieldColor",
					"aerodromeColor", "apronColor", "parkingColor"},
			{"hospital", "amenityHospitalColor"},
			{"school", "amenityEducationalColor"},
			{"water", "waterColor"},
			{"waterLine", "waterStrokeColorNight"},
			{"park", "parkColor", "grassColor", "grasslandColor", "meadowColor", "heathColor", "farmColor", "gardenColor",
					"greeneryColor", "villageGreenColor", "recreationGroundColor", "leisureCommonColor", "pitchColor",
					"playgroundColor", "stadiumColor", "golfCourseColor", "cemeteryColor", "landuseOrchardColor",
					"landuseVineyardColor", "landuseAllotmentsColor"},
			{"wood", "woodColor", "forestColor"},
			{"motorway", "motorwayRoadColor", "motorwayRoadLowZoomColor"},
			{"motorwayCase", "motorwayRoadShadowColor"},
			{"trunk", "trunkRoadColor", "trunkRoadLowZoomColor"},
			{"trunkCase", "trunkRoadShadowColor"},
			{"primary", "primaryRoadColor", "primaryRoadLowZoomColor"},
			{"primaryCase", "primaryRoadShadowColor"},
			{"secondary", "secondaryRoadColor", "secondaryRoadLowZoomColor"},
			{"secondaryCase", "secondaryRoadShadowColor", "tertiaryRoadShadowColor", "tertiaryRoadLowZoomShadowColor"},
			{"minor", "tertiaryRoadColor", "tertiaryRoadLowZoomColor", "residentialRoadColor", "roadRoadColor",
					"primaryResidentialHighwayAreaColor"},
			{"minorCase", "residentialRoadShadowColor", "residentialRoadLowZoom1ShadowColor",
					"residentialRoadLowZoom2ShadowColor", "serviceRoadShadowColor", "serviceDrivewayRoadShadowColor",
					"serviceHighwayAreaShadowColor", "pedestrianRoadShadowColor"},
			{"service", "serviceRoadColor", "serviceDrivewayRoadColor", "serviceHighwayAreaColor", "pedestrianRoadColor"},
			{"path", "footwayColor", "pathColor", "pathLowZoomColor", "stepsRampColor"},
			{"rail", "railwayRailColor"},
			{"oneway", "motorwayHighwayOnewayArrowsColor", "trunkHighwayOnewayArrowsColor",
					"primaryHighwayOnewayArrowsColor", "secondaryHighwayOnewayArrowsColor",
					"tertiaryHighwayOnewayArrowsColor", "residentialHighwayOnewayArrowsColor",
					"serviceHighwayOnewayArrowsColor"},
			{"boundary", "boundaryColorInner"},
			{"placeText", "placeCountryTextColor", "placeStateProvinceTextColor", "placeCityVillageTextColor"},
			{"minorText", "placeSuburbHamletTextColor", "motorwayTextColor", "trunkTextColor", "primaryTextColor",
					"secondaryTextColor", "tertiaryTextColor", "residentialTextColor", "serviceTextColor",
					"pedestrianTextColor", "buildingTextColor"},
			{"halo", "placeCountryTextHaloColor", "placeStateProvinceTextHaloColor", "placeCityVillageTextHaloColor",
					"residentialTextHaloColor", "serviceTextHaloColor", "serviceDrivewayTextHaloColor",
					"buildingTextHaloColor", "waterTextHaloColor", "landuseManmadeTextHaloColor"},
			{"waterText", "waterTextColor"},
	};

	private static String cachedKey;
	private static String cachedXml;

	public static boolean isSupported() {
		return DynamicColors.isDynamicColorAvailable();
	}

	/** Style name that changes with the palette, for caches keyed by name (OpenGL core). */
	@NonNull
	public static String versionedName(@NonNull Context ctx) {
		return RendererRegistry.MATERIAL_RENDER + " " + paletteKey(ctx);
	}

	public static boolean isStale(@NonNull Context ctx) {
		return isSupported() && !paletteKey(ctx).equals(cachedKey);
	}

	@NonNull
	public static synchronized String xml(@NonNull Context ctx) {
		String key = paletteKey(ctx);
		if (!key.equals(cachedKey)) {
			cachedXml = generate(ctx);
			cachedKey = key;
		}
		return cachedXml;
	}

	@NonNull
	private static String paletteKey(@NonNull Context ctx) {
		if (!isSupported()) {
			return "";
		}
		int[] ids = {android.R.color.system_accent1_500, android.R.color.system_accent2_500,
				android.R.color.system_accent3_500, android.R.color.system_neutral1_500,
				android.R.color.system_neutral2_500};
		StringBuilder sb = new StringBuilder();
		for (int id : ids) {
			sb.append(Integer.toHexString(ctx.getColor(id) & 0xFFFFFF));
		}
		return Integer.toHexString((sb.toString() + contrast(ctx)).hashCode());
	}

	private static double contrast(@NonNull Context ctx) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
			UiModeManager uiModeManager = (UiModeManager) ctx.getSystemService(Context.UI_MODE_SERVICE);
			return uiModeManager.getContrast();
		}
		return 0;
	}

	@RequiresApi(api = Build.VERSION_CODES.S)
	@NonNull
	private static DynamicScheme deviceScheme(@NonNull Context ctx, boolean dark) {
		// Tone 50 of each system palette carries the palette's hue and chroma
		Hct primary = Hct.Companion.fromInt(ctx.getColor(android.R.color.system_accent1_500));
		ColorSpec.SpecVersion spec = Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA
				? ColorSpec.SpecVersion.SPEC_2025 : ColorSpec.SpecVersion.SPEC_2021;
		return new DynamicScheme(primary, Variant.TONAL_SPOT, dark, contrast(ctx), DynamicScheme.Platform.PHONE, spec,
				palette(ctx, android.R.color.system_accent1_500),
				palette(ctx, android.R.color.system_accent2_500),
				palette(ctx, android.R.color.system_accent3_500),
				palette(ctx, android.R.color.system_neutral1_500),
				palette(ctx, android.R.color.system_neutral2_500),
				null);
	}

	@NonNull
	private static TonalPalette palette(@NonNull Context ctx, int colorId) {
		Hct hct = Hct.Companion.fromInt(ctx.getColor(colorId));
		return TonalPalette.Companion.fromHueAndChroma(hct.getHue(), hct.getChroma());
	}

	@NonNull
	private static String generate(@NonNull Context ctx) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
			throw new IllegalStateException("Material Design map style needs Android 12+");
		}
		Map<String, Token> day = MaterialMapPalette.tokens(deviceScheme(ctx, false));
		Map<String, Token> night = MaterialMapPalette.tokens(deviceScheme(ctx, true));
		String land = hex(day, "land");
		StringBuilder sb = new StringBuilder("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n")
				.append("<renderingStyle name=\"").append(RendererRegistry.MATERIAL_RENDER)
				.append("\" depends=\"default\" defaultColor=\"").append(land).append("\" version=\"1\">\n")
				.append("\t<renderingAttribute name=\"defaultColor\">\n")
				.append("\t\t<case noPolygons=\"true\" attrColorValue=\"#00").append(land.substring(1)).append("\"/>\n")
				.append("\t\t<case nightMode=\"true\" attrColorValue=\"").append(hex(night, "land")).append("\"/>\n")
				.append("\t\t<case attrColorValue=\"").append(land).append("\"/>\n")
				.append("\t</renderingAttribute>\n");
		for (String[] row : ATTRIBUTES) {
			for (int i = 1; i < row.length; i++) {
				sb.append("\t<renderingAttribute name=\"").append(row[i]).append("\">\n")
						.append("\t\t<case nightMode=\"true\" attrColorValue=\"").append(hex(night, row[0])).append("\"/>\n")
						.append("\t\t<case attrColorValue=\"").append(hex(day, row[0])).append("\"/>\n")
						.append("\t</renderingAttribute>\n");
			}
		}
		return sb.append("</renderingStyle>\n").toString();
	}

	@NonNull
	private static String hex(@NonNull Map<String, Token> tokens, @NonNull String key) {
		Token token = tokens.get(key);
		if (token == null) {
			throw new IllegalArgumentException("Unknown map token " + key);
		}
		return token.getHex().toLowerCase();
	}
}
