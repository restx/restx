package restx.security;

import javax.inject.Named;

import com.google.common.base.Optional;

import restx.common.Crypto;
import restx.factory.Component;

/**
 * Default cookie signer, using HMAC-SHA1 algorithm to sign the cookie.
 *
 * @author apeyrard
 */
@Component
@Named(RestxSessionCookieFilter.COOKIE_SIGNER_NAME)
public class DefaultCookieSigner implements Signer {
	private final SignatureKey signatureKey;

	public DefaultCookieSigner(Optional<SignatureKey> signatureKey) {
		this.signatureKey = signatureKey.or(SignatureKey.DEFAULT);
	}

	@Override
	public String sign(String cookie) {
		return Crypto.sign(cookie, signatureKey.getKey());
	}

	@Override
	public boolean verify(String cookie, String signedCookie) {
		return sign(cookie).equals(signedCookie);
	}
}
