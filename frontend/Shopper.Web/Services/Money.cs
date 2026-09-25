using System.Globalization;

namespace Shopper.Web.Services;

/// <summary>
/// Formats money for display. The API sends <b>integer pence</b> (ADR-005), and this is the
/// <b>only</b> place pence become pounds (000 §5.3: "£" symbol, two decimal places).
/// </summary>
public static class Money
{
    /// <summary>120 → "£1.20", 5 → "£0.05", 999999 → "£9,999.99".</summary>
    public static string Format(int pence)
    {
        // decimal, not double: exact for money. Invariant culture, so the output never depends
        // on the browser's language settings.
        var pounds = Math.Abs(pence) / 100m;
        var sign = pence < 0 ? "-" : "";
        return $"{sign}£{pounds.ToString("N2", CultureInfo.InvariantCulture)}";
    }
}
